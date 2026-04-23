package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EstoqueFalhouEvent (
        EventMetadata metadata,
       UUID pedidoId,
       String motivo
)implements PedidoEvent{}
