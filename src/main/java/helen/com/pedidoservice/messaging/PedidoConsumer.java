package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.service.EventProcessingService;
import helen.com.pedidoservice.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoConsumer {

    private final PedidoService service;
    private final EventProcessingService eventProcessingService;

    @RabbitListener(queues = "estoque.sucesso")
    public void estoqueSucesso(EstoqueConfirmadoEvent evento) {
        log.info("Mensagem recebida queue=estoque.sucesso pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "estoque.sucesso", () -> service.processarEstoqueSucesso(evento));
    }

    @RabbitListener(queues = "estoque.falha")
    public void estoqueFalha(EstoqueFalhouEvent evento) {
        log.info("Mensagem recebida queue=estoque.falha pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "estoque.falha", () -> service.processarEstoqueFalha(evento));
    }

    @RabbitListener(queues = "email.sucesso")
    public void emailSucesso(EmailEnviadoEvent evento) {
        log.info("Mensagem recebida queue=email.sucesso pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "email.sucesso", () -> service.processarEmailSucesso(evento));
    }

    @RabbitListener(queues = "email.falha")
    public void emailFalha(EmailFalhouEvent evento) {
        log.info("Mensagem recebida queue=email.falha pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "email.falha", () -> service.processarEmailFalha(evento));
    }

    @RabbitListener(queues = "nota.sucesso")
    public void notaSucesso(NotaEmitidaEvent evento) {
        log.info("Mensagem recebida queue=nota.sucesso pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "nota.sucesso", () -> service.finalizarPedido(evento));
    }

    @RabbitListener(queues = "nota.falha")
    public void notaFalha(NotaFalhouEvent evento) {
        log.info("Mensagem recebida queue=nota.falha pedidoId={} correlationId={} traceId={}",
                evento.pedidoId(), evento.metadata().correlationId(), evento.metadata().traceId());
        eventProcessingService.executeIfNew(evento, "nota.falha", () -> service.processarNotaFalha(evento));
    }
}
