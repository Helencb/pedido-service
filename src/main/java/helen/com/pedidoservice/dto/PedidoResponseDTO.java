package helen.com.pedidoservice.dto;

import helen.com.pedidoservice.model.StatusPedido;

import java.util.List;
import java.util.UUID;

public record PedidoResponseDTO (
        UUID id,
        UUID clienteId,
        StatusPedido status,
        List<String> itens
){}

