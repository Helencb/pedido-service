package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EmailSolicitadoEvent (
        EventMetadata metadata,
        UUID pedidoId
)implements PedidoEvent{}

