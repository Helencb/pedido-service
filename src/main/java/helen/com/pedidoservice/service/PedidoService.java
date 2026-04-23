package helen.com.pedidoservice.service;

import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.exception.BusinessException;
import helen.com.pedidoservice.exception.ResourceNotFoundException;
import helen.com.pedidoservice.mapper.PedidoMapper;
import helen.com.pedidoservice.messaging.PedidoProducer;
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

        EventMetadata metadata  = EventMetadata.create(null, null);
        PedidoCriadoEvent evento = new PedidoCriadoEvent(
                metadata,
                pedido.getId(),
                pedido.getClienteId(),
                dto.itens()
        );

        producer.enviarParaEstoque(evento);

        log.info("Pedido criado pedidoId={} status={} correlationId={}",
                pedido.getId(), pedido.getStatus(), metadata.correlationId());
        return mapper.toDTO(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(UUID id) {
        return mapper.toDTO(buscarPedido(id));
    }

    public Page<PedidoResponseDTO> listar(Pageable pageable) {
        return repository.findByAtivoTrue(pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public PedidoResponseDTO atualizar(UUID id, PedidoUpdateDTO dto) {
        Pedido pedido = buscarPedido(id);
        validarPedidoEditavel(pedido);

        mapper.applyUpdate(pedido, dto);
        repository.save(pedido);

        log.info("Pedido atualizado integralmente pedidoId={}", id);
        return mapper.toDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO atualizarParcial(UUID id, PedidoPatchDTO dto) {
        Pedido pedido = buscarPedido(id);
        validarPedidoEditavel(pedido);

        if (dto.clienteId() != null) {
            pedido.setClienteId(dto.clienteId());
        }
        if (dto.itens() != null) {
            pedido.setItens(mapper.toItemEntities(dto.itens()));
        }

        repository.save(pedido);

        log.info("Pedido atualizado parcialmente pedidoId={}", id);
        return mapper.toDTO(pedido);
    }

    @Transactional
    public void cancelar(UUID id) {
        Pedido pedido = buscarPedido(id);

        if(pedido.getStatus() == StatusPedido.FINALIZADO) {
            throw new BusinessException("Pedido finalizado não pode ser cancelado");
        }
        if(pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessException("Pedido ja esta cancelado");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
        log.info("Pedido cancelado {}", id);
    }

    @Transactional
    public void processarEstoqueSucesso(EstoqueConfirmadoEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_ESTOQUE) {
            log.warn("Pedido ignorado no estoque.sucesso pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_EMAIL);
        repository.save(pedido);

        EventMetadata metadata = EventMetadata.create(evento.metadata().correlationId(), evento.metadata().traceId());
        producer.enviarEmail(new EmailSolicitadoEvent(metadata, evento.pedidoId()));

        log.info("Pedido avancou para AGUARDANDO_EMAIL pedidoId={} correlationId={}",
                evento.pedidoId(), metadata.correlationId());
    }

    @Transactional
    public void processarEstoqueFalha(EstoqueFalhouEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_ESTOQUE) {
            log.warn("Pedido ignorado no estoque.falha pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        log.error("Pedido cancelado por falha de estoque pedidoId={} motivo={} correlationId={}",
                evento.pedidoId(), evento.motivo(), evento.metadata().correlationId());
    }

    @Transactional
    public void processarEmailSucesso(EmailEnviadoEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_EMAIL) {
            log.warn("Pedido ignorado no email.sucesso pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_NOTA);
        repository.save(pedido);

        EventMetadata metadata = EventMetadata.create(evento.metadata().correlationId(), evento.metadata().traceId());
        producer.enviarNota(new NotaSolicitadaEvent(metadata, evento.pedidoId()));

        log.info("Pedido avancou para AGUARDANDO_NOTA pedidoId={} correlationId={}",
                evento.pedidoId(), metadata.correlationId());
    }

    @Transactional
    public void processarEmailFalha(EmailFalhouEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_EMAIL) {
            log.warn("Pedido ignorado no email.falha pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        log.error("Pedido cancelado por falha de email pedidoId={} motivo={} correlationId={}",
                evento.pedidoId(), evento.motivo(), evento.metadata().correlationId());
    }

    @Transactional
    public void finalizarPedido(NotaEmitidaEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_NOTA) {
            log.warn("Pedido ignorado no nota.sucesso pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.FINALIZADO);
        repository.save(pedido);

        log.info("Pedido finalizado pedidoId={} correlationId={}",
                evento.pedidoId(), evento.metadata().correlationId());
    }

    @Transactional
    public void processarNotaFalha(NotaFalhouEvent evento) {

        Pedido pedido = buscarPedido(evento.pedidoId());

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_NOTA) {
            log.warn("Pedido ignorado no nota.falha pedidoId={} statusAtual={}", evento.pedidoId(), pedido.getStatus());
            return;
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        log.error("Pedido cancelado por falha de nota pedidoId={} motivo={} correlationId={}",
                evento.pedidoId(), evento.motivo(), evento.metadata().correlationId());
    }

    private Pedido buscarPedido(UUID id) {
        return repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    private void validarPedidoEditavel(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_ESTOQUE) {
            throw new BusinessException("Pedido nao pode ser alterado nesse estado");
        }
    }
}
