package helen.com.pedidoservice.service;

import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.exception.BusinessException;
import helen.com.pedidoservice.exception.ResourceNotFoundException;
import helen.com.pedidoservice.mapper.PedidoMapper;
import helen.com.pedidoservice.messaging.PedidoProducer;
import helen.com.pedidoservice.model.ItemPedido;
import helen.com.pedidoservice.model.Pedido;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final PedidoProducer producer;

    @Transactional
    public PedidoResponseDTO criar(PedidoCreateDTO dto){
        Pedido pedido = mapper.toEntity(dto);

        pedido.setStatus(StatusPedido.AGUARDANDO_ESTOQUE);

        pedido = repository.save(pedido);

        PedidoCriadoEvent evento = new PedidoCriadoEvent(
                pedido.getId(),
                pedido.getClienteId(),
                dto.itens()
        );

        producer.enviarParaEstoque(evento);

        log.info("Pedido criado com sucesso {}", pedido.getId());
        return mapper.toDTO(pedido);
    }

    public PedidoResponseDTO buscarPorId(UUID id) {
        log.info("Buscando pedido {}", id);
        Pedido pedido = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));

        return mapper.toDTO(pedido);
    }

    public Page<PedidoResponseDTO> listar(Pageable pageable) {
        return repository.findByAtivoTrue(pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public PedidoResponseDTO atualizar(UUID id, PedidoUpdateDTO dto) {
        Pedido pedido = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if(!pedido.getStatus().equals(StatusPedido.AGUARDANDO_ESTOQUE)) {
            throw new BusinessException("Pedido não pode ser alterado nesse estado");
        }

        pedido.setClienteId(dto.clienteId());

        pedido.setItens(dto.itens().stream().map(i -> {
            ItemPedido item = new ItemPedido();
            item.setProdutoId(i.produtoId());
            item.setNome(i.nome());
            item.setQuantidade(i.quantidade());

            item.setPedido(pedido);

            return item;
        }).toList());

        repository.save(pedido);
        log.info("Pedido atualizado {}", id);
        return mapper.toDTO(pedido);
    }

    @Transactional
    public void cancelar(UUID id) {
        Pedido pedido = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
        log.info("Pedido cancelado {}", id);
    }

    @Transactional
    public void processarEstoqueSucesso(EstoqueConfirmadoEvent evento) {

        UUID pedidoId = evento.pedidoId();

        Pedido pedido = buscarPedido(pedidoId);

        if(!pedido.getStatus().equals(StatusPedido.AGUARDANDO_ESTOQUE)) {
            log.warn("Pedido {} ignorado - estado inválido {}", pedidoId, pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_EMAIL);
        repository.save(pedido);

       producer.enviarEmail(new EmailSolicitadoEvent(
                pedidoId
        ));

        log.info("Pedido {} atualizado para AGUARDANDO_EMAIL", pedidoId);
    }

    @Transactional
    public void processarEstoqueFalha(EstoqueFalhouEvent evento) {

        UUID pedidoId = evento.pedidoId();

        log.error("Falha no estoque para pedido {} - Motivo: {}",
                pedidoId, evento.motivo());

        Pedido pedido = buscarPedido(pedidoId);

        pedido.setStatus(StatusPedido.CANCELADO);

        repository.save(pedido);

        log.info("Pedido {} cancelado por falha no estoque", pedidoId);
    }

    @Transactional
    public void processarEmailSucesso(EmailEnviadoEvent evento) {

        UUID pedidoId = evento.pedidoId();

        log.info("Email enviado para pedido {}", pedidoId);

        Pedido pedido = buscarPedido(pedidoId);

        if (!pedido.getStatus().equals(StatusPedido.AGUARDANDO_EMAIL)) {
            log.warn("Pedido {} ignorado no email - estado {}", pedidoId, pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_NOTA);
        repository.save(pedido);

        NotaSolicitadaEvent notaEvent = new NotaSolicitadaEvent(pedidoId);
        producer.enviarNota(notaEvent);
    }

    @Transactional
    public void processarEmailFalha(EmailFalhouEvent evento) {

        UUID pedidoId = evento.pedidoId();

        log.error("Falha ao enviar email do pedido {} - Motivo: {}", pedidoId, evento.motivo());

        Pedido pedido = buscarPedido(pedidoId);

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
    }

    @Transactional
    public void finalizarPedido(NotaEmitidaEvent evento) {

        UUID pedidoId = evento.pedidoId();

        log.info("Nota emitida para pedido {}", pedidoId);

        Pedido pedido = buscarPedido(pedidoId);

        if (!pedido.getStatus().equals(StatusPedido.AGUARDANDO_NOTA)) {
            log.warn("Pedido {} ignorado na nota - estado {}", pedidoId, pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.FINALIZADO);
        repository.save(pedido);

        log.info("Pedido {} FINALIZADO", pedidoId);
    }

    @Transactional
    public void processarNotaFalha(NotaFalhouEvent evento) {
        UUID pedidoId = evento.pedidoId();

        Pedido pedido = buscarPedido(pedidoId);

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        log.error("Pedido {} cancelado por falha na nota", pedidoId);
    }

    private Pedido buscarPedido(UUID id) {
        return repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

}
