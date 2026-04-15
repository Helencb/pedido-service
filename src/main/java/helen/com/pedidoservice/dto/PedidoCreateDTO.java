package helen.com.pedidoservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PedidoCreateDTO(
        @NotNull(message = "Cliente é obrigatório")
        UUID clienteId,

        @NotEmpty(message = "Pedido deve ter itens")
        List<ItemPedidoDTO> itens
)
{}
