package helen.com.pedidoservice.messaging;

import helen.com.pedidoservice.dto.EstoqueConfirmadoEvent;
import helen.com.pedidoservice.dto.EventMetadata;
import helen.com.pedidoservice.service.EventProcessingService;
import helen.com.pedidoservice.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PedidoConsumerTest {
    @Mock
    private PedidoService pedidoService;

    @Mock
    private EventProcessingService eventProcessingService;

    @InjectMocks
    private PedidoConsumer pedidoConsumer;

    @Test
    void deveDelegarDeduplicacaoAoConsumirEvento() {
        EstoqueConfirmadoEvent event = new EstoqueConfirmadoEvent(EventMetadata.create("corr", "trace"), java.util.UUID.randomUUID());

        pedidoConsumer.estoqueSucesso(event);

        verify(eventProcessingService).executeIfNew(eq(event), eq("estoque.sucesso"), any(Runnable.class));
    }
}
