package helen.com.pedidoservice.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;


public record ItemPedidoDTO(
        @NotNull(message = "Produto e obrigatorio")
        UUID produtoId,

        @NotBlank(message = "Nome do item e obrigatorio")
        @Size(max = 120, message = "Nome do item deve ter no maximo 120 caracteres")
        String nome,

        @NotNull(message = "Quantidade e obrigatoria")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        @Max(value = 1000, message = "Quantidade deve ser no maximo 1000")
        Integer quantidade
) {}
