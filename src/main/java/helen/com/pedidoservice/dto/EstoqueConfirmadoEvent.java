package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EstoqueConfirmadoEvent(
        UUID pedidoId
) {}
