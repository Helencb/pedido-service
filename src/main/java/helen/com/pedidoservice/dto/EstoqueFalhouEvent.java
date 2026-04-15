package helen.com.pedidoservice.dto;

import java.util.UUID;

public record EstoqueFalhouEvent (
       UUID pedidoId,
       String motivo
){}
