package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.EmailSolicitadoEvent;
import helen.com.pedidoservice.dto.NotaSolicitadaEvent;
import helen.com.pedidoservice.dto.PedidoCriadoEvent;
import helen.com.pedidoservice.service.OutboxPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoProducer {
    private final RabbitTemplate rabbitTemplate;

    private static final String RK_ESTOQUE = "estoque.solicitar";
    private static final String RK_EMAIL = "email.solicitar";
    private static final String RK_NOTA = "nota.solicitar";

    private final OutboxPublisherService outboxPublisherService;

    public void enviarParaEstoque(PedidoCriadoEvent evento) {
        outboxPublisherService.store(evento, RK_ESTOQUE);
        log.info("Evento de pedido criado persistido no outbox pedidoId={} correlationId={}",
                evento.pedidoId(), evento.metadata().correlationId());
    }

    public void enviarEmail(EmailSolicitadoEvent evento) {
        outboxPublisherService.store(evento, RK_EMAIL);
        log.info("Evento de email persistido no outbox pedidoId={} correlationId={}",
                evento.pedidoId(), evento.metadata().correlationId());
    }


    public void enviarNota(NotaSolicitadaEvent evento) {
        outboxPublisherService.store(evento, RK_NOTA);
        log.info("Evento de nota persistido no outbox pedidoId={} correlationId={}",
                evento.pedidoId(), evento.metadata().correlationId());
    }
}
