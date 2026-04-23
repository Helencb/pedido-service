package helen.com.pedidoservice.dto;

import java.util.UUID;

public interface PedidoEvent {
    EventMetadata metadata();
    UUID pedidoId();
}
