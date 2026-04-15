package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.EmailEnviadoEvent;
import helen.com.pedidoservice.dto.EmailFalhouEvent;
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
public class EmailListener {

    private final PedidoRepository repository;
    private final RabbitTemplate rabbit;

    @RabbitListener(queues = "email.sucesso")
    public void sucesso(EmailEnviadoEvent evento) {

        log.info("Email enviado para pedido {}", evento.pedidoId());

        Pedido pedido = repository.findById(evento.pedidoId())
                        .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));
        pedido.setStatus(StatusPedido.AGUARDANDO_NOTA);
        repository.save(pedido);

        rabbit.convertAndSend(
                "pedido.exchange",
                "nota.solicitar",
                evento
        );
    }

    @RabbitListener(queues = "email.falha")
    public void falha(EmailFalhouEvent evento){
        log.error("Email falhou para pedido {}", evento.pedidoId());

        Pedido pedido = repository.findById(evento.pedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(StatusPedido.CANCELADO);

        repository.save(pedido);
    }
}
