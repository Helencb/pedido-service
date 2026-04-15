package helen.com.pedidoservice.controller;

import helen.com.pedidoservice.dto.ApiResponse;
import helen.com.pedidoservice.dto.PedidoCreateDTO;
import helen.com.pedidoservice.dto.PedidoResponseDTO;
import helen.com.pedidoservice.dto.PedidoUpdateDTO;
import helen.com.pedidoservice.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> criar(@Valid @RequestBody PedidoCreateDTO dto) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Pedido criado com sucesso", service.criar(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido encontrado", service.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PedidoResponseDTO>>> listar(Pageable pageable) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de pedidos", service.listar(pageable)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody PedidoUpdateDTO dto){
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido atualziado", service.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido cancelado", null));
    }
}
