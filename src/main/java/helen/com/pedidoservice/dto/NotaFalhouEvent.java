package helen.com.pedidoservice.dto;

import java.util.UUID;

public record NotaFalhouEvent (
         UUID pedidoId,
         String motivo
){}
