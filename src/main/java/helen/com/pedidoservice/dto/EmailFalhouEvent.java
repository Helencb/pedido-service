package helen.com.pedidoservice.dto;
import java.util.UUID;

public record EmailFalhouEvent (
      UUID pedidoId,
      String motivo
){}
