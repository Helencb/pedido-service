package helen.com.pedidoservice.integration;

import helen.com.pedidoservice.client.ProdutoClient;
import helen.com.pedidoservice.client.ProdutoDTO;
import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.model.OutboxStatus;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.repository.OutboxEventRepository;
import helen.com.pedidoservice.repository.PedidoRepository;
import helen.com.pedidoservice.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class PedidoFluxoIntegrationTest {
    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private ProdutoClient produtoClient;

    @Test
    void deveExecutarFluxoCompletoSemPublicarAutomaticamente() {
        UUID clienteId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        PedidoCreateDTO createDTO = new PedidoCreateDTO(
                clienteId,
                List.of(new ItemPedidoDTO(produtoId, "Notebook", 1))
        );

        when(produtoClient.buscarPorId(any()))
                .thenReturn(new ApiResponse<>(true, "ok", new ProdutoDTO(produtoId, "Notebook", null, true)));

        PedidoResponseDTO criado = pedidoService.criar(createDTO);
        assertEquals(StatusPedido.AGUARDANDO_ESTOQUE, criado.status());
        assertTrue(outboxEventRepository.findAll().stream().anyMatch(event -> event.getStatus() == OutboxStatus.PENDING));

        pedidoService.processarEstoqueSucesso(new EstoqueConfirmadoEvent(EventMetadata.create("corr-1", "trace-1"), criado.id()));
        assertEquals(StatusPedido.AGUARDANDO_EMAIL, pedidoRepository.findById(criado.id()).orElseThrow().getStatus());

        pedidoService.processarEmailSucesso(new EmailEnviadoEvent(EventMetadata.create("corr-1", "trace-1"), criado.id()));
        assertEquals(StatusPedido.AGUARDANDO_NOTA, pedidoRepository.findById(criado.id()).orElseThrow().getStatus());

        pedidoService.finalizarPedido(new NotaEmitidaEvent(EventMetadata.create("corr-1", "trace-1"), criado.id()));
        assertEquals(StatusPedido.FINALIZADO, pedidoRepository.findById(criado.id()).orElseThrow().getStatus());
    }
}
