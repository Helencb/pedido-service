package helen.com.pedidoservice.dto;

import java.util.UUID;

public record NotaEmitidaEvent(
        EventMetadata metadata,
        UUID pedidoId
)implements PedidoEvent{}
