package helen.com.pedidoservice;

import helen.com.pedidoservice.dto.EstoqueConfirmadoEvent;
import helen.com.pedidoservice.dto.EstoqueFalhouEvent;
import helen.com.pedidoservice.messaging.PedidoConsumer;
import helen.com.pedidoservice.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;


import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PedidoConsumerTest {
    @Mock
    private PedidoService service;

    @InjectMocks
    private PedidoConsumer consumer;

    @Test
    void deveProcessarEstoqueSucesso() {
        EstoqueConfirmadoEvent evento = new EstoqueConfirmadoEvent();

        consumer.estoqueSucesso(evento);

        verify(service, times(1)).processarEstoqueSucesso(evento);
    }

    @Test
    void deveProcessarEstoqueFalha() {
        EstoqueFalhouEvent evento = new EstoqueFalhouEvent();

        consumer.estoqueFalha(evento);

        verify(service, times(1)).processarEstoqueFalha(evento);
    }
}
