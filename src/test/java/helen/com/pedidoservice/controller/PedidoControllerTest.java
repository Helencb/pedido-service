package helen.com.pedidoservice.controller;

import helen.com.pedidoservice.dto.ItemPedidoDTO;
import helen.com.pedidoservice.dto.PedidoCreateDTO;
import helen.com.pedidoservice.dto.PedidoPatchDTO;
import helen.com.pedidoservice.dto.PedidoResponseDTO;
import helen.com.pedidoservice.exception.GlobalExceptionHandler;
import helen.com.pedidoservice.model.StatusPedido;
import helen.com.pedidoservice.service.PedidoService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;



import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "app.api.pagination.default-size=20",
        "app.api.pagination.max-size=100"
})
public class PedidoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void deveRetornar201EHeaderLocationAoCriarPedido() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        PedidoCreateDTO dto = new PedidoCreateDTO(clienteId, List.of(new ItemPedidoDTO(UUID.randomUUID(), "Mouse", 1)));

        when(pedidoService.criar(dto)).thenReturn(new PedidoResponseDTO(id, clienteId, StatusPedido.AGUARDANDO_ESTOQUE, List.of("Mouse x1")));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/pedidos/" + id))
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    void deveRetornar400QuandoPatchNaoTemCampos() throws Exception {
        PedidoPatchDTO dto = new PedidoPatchDTO(null, null);

        mockMvc.perform(patch("/pedidos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }
}

