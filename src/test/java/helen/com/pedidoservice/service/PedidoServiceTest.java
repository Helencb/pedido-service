package helen.com.pedidoservice.service;

import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.exception.BusinessException;
import helen.com.pedidoservice.mapper.PedidoMapper;
import helen.com.pedidoservice.messaging.PedidoProducer;
import helen.com.pedidoservice.model.ItemPedido;
import helen.com.pedidoservice.model.Pedido;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PedidoServiceTest {
    @Mock
    private PedidoRepository repository;

    @Mock
    private PedidoMapper mapper;

    @Mock
    private PedidoProducer producer;

    @InjectMocks
    private PedidoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarPedidoEEnviarEvento() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ItemPedidoDTO itemDTO = new ItemPedidoDTO(UUID.randomUUID(), "Camiseta", 2);
        PedidoCreateDTO dto = new PedidoCreateDTO(clienteId, List.of(itemDTO));

        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setClienteId(clienteId);
        pedido.setItens(List.of(new ItemPedido()));

        when(mapper.toEntity(dto)).thenReturn(pedido);
        when(repository.save(any(Pedido.class))).thenReturn(pedido);
        when(mapper.toDTO(pedido)).thenReturn(new PedidoResponseDTO(id, clienteId, StatusPedido.AGUARDANDO_ESTOQUE, List.of("Camiseta x2")));

        PedidoResponseDTO resultado = service.criar(dto);

        assertEquals(StatusPedido.AGUARDANDO_ESTOQUE, pedido.getStatus());
        verify(repository, times(1)).save(pedido);
        verify(producer, times(1)).enviarParaEstoque(any(PedidoCriadoEvent.class));
        assertNotNull(resultado);
        assertEquals(pedido, pedido.getItens().get(0).getPedido());
    }

    @Test
    void deveBloquearCancelamentoDePedidoFinalizado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.FINALIZADO);

        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(pedido));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.cancelar(id));

        assertTrue(exception.getMessage().contains("finalizado"));
        verify(repository, never()).save(any(Pedido.class));
    }

    @Test
    void deveAtualizarParcialmenteItensQuandoPedidoEditavel() {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.AGUARDANDO_ESTOQUE);
        pedido.setItens(List.of(new ItemPedido()));

        PedidoPatchDTO patchDTO = new PedidoPatchDTO(null, List.of(new ItemPedidoDTO(UUID.randomUUID(), "Tenis", 1)));
        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(pedido));
        when(mapper.toItemEntities(patchDTO.itens())).thenReturn(List.of(new ItemPedido()));
        when(mapper.toDTO(pedido)).thenReturn(new PedidoResponseDTO(id, null, StatusPedido.AGUARDANDO_ESTOQUE, List.of("Tenis x1")));

        PedidoResponseDTO response = service.atualizarParcial(id, patchDTO);

        verify(repository).save(pedido);
        assertNotNull(response);
    }

    @Test
    void deveIgnorarFalhaDeEstoqueQuandoPedidoJaAvancou() {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.AGUARDANDO_EMAIL);

        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(pedido));

        service.processarEstoqueFalha(new EstoqueFalhouEvent(EventMetadata.create("corr", "trace"), id, "timeout"));

        verify(repository, never()).save(any(Pedido.class));
        assertEquals(StatusPedido.AGUARDANDO_EMAIL, pedido.getStatus());
    }

    @Test
    void deveIgnorarFalhaDeEmailQuandoPedidoJaFoiFinalizado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.FINALIZADO);

        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(pedido));

        service.processarEmailFalha(new EmailFalhouEvent(EventMetadata.create("corr", "trace"), id, "smtp"));

        verify(repository, never()).save(any(Pedido.class));
        assertEquals(StatusPedido.FINALIZADO, pedido.getStatus());
    }

    @Test
    void deveIgnorarFalhaDeNotaQuandoPedidoJaFoiCancelado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.CANCELADO);

        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(pedido));

        service.processarNotaFalha(new NotaFalhouEvent(EventMetadata.create("corr", "trace"), id, "sefaz"));

        verify(repository, never()).save(any(Pedido.class));
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }
}
