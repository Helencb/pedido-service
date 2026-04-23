package helen.com.pedidoservice.repository;

import helen.com.pedidoservice.model.ItemPedido;
import helen.com.pedidoservice.model.Pedido;
import helen.com.pedidoservice.model.StatusPedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class PedidoRepositoryIntegrationTest {
    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void devePersistirPedidoComItensUsandoUuid() {
        Pedido pedido = new Pedido();
        pedido.setClienteId(UUID.randomUUID());
        pedido.setStatus(StatusPedido.AGUARDANDO_ESTOQUE);

        ItemPedido item = new ItemPedido();
        item.setProdutoId(UUID.randomUUID());
        item.setNome("Teclado");
        item.setQuantidade(2);

        pedido.setItens(List.of(item));

        Pedido salvo = pedidoRepository.saveAndFlush(pedido);

        assertTrue(salvo.getId() != null);
        assertEquals(1, salvo.getItens().size());
        assertEquals(salvo, salvo.getItens().get(0).getPedido());
    }
}
