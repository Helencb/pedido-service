package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.config.RabbitConfig;
import helen.com.pedidoservice.dto.EmailSolicitadoEvent;
import helen.com.pedidoservice.dto.NotaSolicitadaEvent;
import helen.com.pedidoservice.dto.PedidoCriadoEvent;
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


    public void enviarParaEstoque(PedidoCriadoEvent evento) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RK_ESTOQUE,
                    evento
            );
            log.info("Evento enviado para estoque: pedido {}", evento.pedidoId());
        } catch (Exception e) {
            log.error("Erro ao enviar evento para estoque: {}", evento.pedidoId(), e);
            throw e;
        }
    }

    public void enviarEmail(EmailSolicitadoEvent evento) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RK_EMAIL,
                    evento
            );
            log.info("Evento enviado para email: pedido {}", evento.pedidoId());
        } catch (Exception e) {
            log.error("Erro ao enviar evento para email: {}", evento.pedidoId(), e);
            throw e;
        }
    }

    public void enviarNota(NotaSolicitadaEvent evento) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RK_NOTA,
                    evento
            );
            log.info("Evento enviado para nota: pedido {}", evento.pedidoId());
        } catch (Exception e) {
            log.error("Erro ao enviar evento para nota: {}", evento.pedidoId(), e);
            throw e;
        }
    }
}
