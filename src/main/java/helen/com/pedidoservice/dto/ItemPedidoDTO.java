package helen.com.pedidoservice.dto;

import java.util.UUID;


public record ItemPedidoDTO(
        UUID produtoId,
        String nome,
        Integer quantidade
) {}
