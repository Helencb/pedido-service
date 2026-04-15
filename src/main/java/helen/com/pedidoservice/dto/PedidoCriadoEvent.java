package helen.com.pedidoservice.dto;

import java.util.List;
import java.util.UUID;

public record PedidoCriadoEvent(
        UUID pedidoId,
        UUID clienteId,
        List<ItemPedidoDTO> itens
) {}
