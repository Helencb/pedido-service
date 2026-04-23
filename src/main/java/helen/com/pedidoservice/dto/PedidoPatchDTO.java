package helen.com.pedidoservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PedidoPatchDTO(
        UUID clienteId,
        @Valid List<ItemPedidoDTO> itens
) {
    @AssertTrue(message = "Informe ao menos um campo para atualizacao")
    public boolean hasAnyField() {
        return clienteId != null || itens != null;
    }

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
