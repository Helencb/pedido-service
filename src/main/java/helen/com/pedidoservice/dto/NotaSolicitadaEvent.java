package helen.com.pedidoservice.dto;

import java.util.UUID;

public record NotaSolicitadaEvent (
        EventMetadata metadata,
        UUID pedidoId
)implements PedidoEvent{}
