package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.NotaEmitidaEvent;
import helen.com.pedidoservice.dto.NotaFalhouEvent;
import helen.com.pedidoservice.model.Pedido;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotaListener {
    private final PedidoRepository repository;

    @RabbitListener(queues = "nota.sucesso")
    public void sucesso(NotaEmitidaEvent evento) {
        log.info("Nota gerada para pedido {}", evento.pedidoId());

        Pedido pedido = repository.findById(evento.pedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(StatusPedido.FINALIZADO);
        repository.save(pedido);

        log.info("Pedido {} CONFIRMADO com sucesso!", evento.pedidoId());
    }

    @RabbitListener(queues = "nota.falha")
    public void falha(NotaFalhouEvent evento) {
        log.error("Falha ao gerar nota para pedido {}", evento.pedidoId());
        Pedido pedido = repository.findById(evento.pedidoId())
                .orElseThrow(() -> {
                    log.error("Pedido {} não encontrado ao cancelar nota", evento.pedidoId());
                    return new RuntimeException("Pedido não encontrado");
                });
        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        log.info("Pedido {} cancelado por falha na nota", evento.pedidoId());
    }
}
