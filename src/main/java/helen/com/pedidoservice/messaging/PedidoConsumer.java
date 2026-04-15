package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.*;
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

    @RabbitListener(queues = "estoque.sucesso")
    public void estoqueSucesso(EstoqueConfirmadoEvent evento) {
        log.info("Mensagem recebida: estoque sucesso {}", evento.pedidoId());
        service.processarEstoqueSucesso(evento);
    }

    @RabbitListener(queues = "estoque.falha")
    public void estoqueFalha(EstoqueFalhouEvent evento) {
        log.info("Mensagem recebida: estoque falha {}", evento.pedidoId());
        service.processarEstoqueFalha(evento);
    }

    @RabbitListener(queues = "email.sucesso")
    public void emailSucesso(EmailEnviadoEvent evento) {
        log.info("Mensagem recebida: email sucesso {}", evento.pedidoId());
        service.processarEmailSucesso(evento);
    }

    @RabbitListener(queues = "email.falha")
    public void emailFalha(EmailFalhouEvent evento) {
        log.info("Mensagem recebida: email falha {}", evento.pedidoId());
        service.processarEmailFalha(evento);
    }

    @RabbitListener(queues = "nota.sucesso")
    public void notaSucesso(NotaEmitidaEvent evento) {
        log.info("Mensagem recebida: nota sucesso {}", evento.pedidoId());
        service.finalizarPedido(evento);
    }

    @RabbitListener(queues = "nota.falha")
    public void notaFalha(NotaFalhouEvent evento) {
        log.info("Mensagem recebida: nota falha {}", evento.pedidoId());
        service.processarNotaFalha(evento);
    }
}
