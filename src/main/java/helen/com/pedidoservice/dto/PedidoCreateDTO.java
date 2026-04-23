package helen.com.pedidoservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PedidoCreateDTO(
        @NotNull(message = "Cliente é obrigatório")
        UUID clienteId,

        @NotEmpty(message = "Pedido deve ter itens")
        @Valid
        List<ItemPedidoDTO> itens
)
{
        @AssertTrue(message = "Pedido nao deve ter produtos duplicados")
        public boolean hasNoDuplicatedProducts() {
                if (itens == null) {
                        return true;
                }

                long distinctCount = itens.stream()
                        .map(ItemPedidoDTO::produtoId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

                return distinctCount == itens.size();
        }
}
