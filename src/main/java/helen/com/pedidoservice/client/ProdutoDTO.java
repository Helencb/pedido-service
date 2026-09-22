package helen.com.pedidoservice.client;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoDTO(
        UUID id,
        String nome,
        BigDecimal valor,
        boolean ativo
) {
}
