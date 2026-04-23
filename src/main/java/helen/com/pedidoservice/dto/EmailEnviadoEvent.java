package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EmailEnviadoEvent (
        EventMetadata metadata,
        UUID pedidoId
)implements PedidoEvent {}
