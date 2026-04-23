package helen.com.pedidoservice.dto;
import java.util.UUID;

public record EmailFalhouEvent (
        EventMetadata metadata,
        UUID pedidoId,
        String motivo
) implements PedidoEvent
{}
