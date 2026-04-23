package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EstoqueConfirmadoEvent(
        EventMetadata metadata,
        UUID pedidoId
) implements PedidoEvent{}
