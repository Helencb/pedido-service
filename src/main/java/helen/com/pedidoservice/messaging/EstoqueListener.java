package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.EmailSolicitadoEvent;
import helen.com.pedidoservice.dto.EstoqueConfirmadoEvent;
import helen.com.pedidoservice.dto.EstoqueFalhouEvent;
import helen.com.pedidoservice.model.Pedido;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EstoqueListener {
    private final PedidoRepository repository;
    private final RabbitTemplate rabbit;

    @RabbitListener(queues = "estoque.sucesso")
    public void sucesso(EstoqueConfirmadoEvent evento) {
        log.info("Estoque confirmado para pedido {}", evento.pedidoId());

        Pedido pedido = repository.findById(evento.pedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        pedido.setStatus(StatusPedido.AGUARDANDO_EMAIL);
        repository.save(pedido);

        rabbit.convertAndSend(
                "pedido.exchange",
                "email.solicitar",
                new EmailSolicitadoEvent(evento.pedidoId())
        );
    }

    @RabbitListener(queues = "estoque.falha")
    public void falha(EstoqueFalhouEvent evento) {
        log.error("Estoque falhou {} motivo: {}", evento.pedidoId(), evento.motivo());

        Pedido pedido = repository.findById(evento.pedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
    }
}
