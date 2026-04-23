package helen.com.pedidoservice.controller;

import helen.com.pedidoservice.dto.*;
import helen.com.pedidoservice.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name  = "Pedidos")
public class PedidoController {
    private final PedidoService service;

    @Value("${app.api.pagination.default-size}")
    private int defaultPageSize;

    @Value("${app.api.pagination.max-size}")
    private int maxPageSize;

    @PostMapping
    @Operation(summary = "Cria um novo pedido")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> criar(@Valid @RequestBody PedidoCreateDTO dto) {
        PedidoResponseDTO response = service.criar(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse<>(true, "Pedido criado com sucesso", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pedido por id")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido encontrado", service.buscarPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Lista pedidos com paginacao controlada")
    public ResponseEntity<ApiResponse<PageResponse<PedidoResponseDTO>>> listar(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(required = false) Integer size,
                                                                          @RequestParam(defaultValue = "id") String sort,
                                                                          @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        int resolvedSize = size == null ? defaultPageSize : Math.min(Math.max(size, 1), maxPageSize);
        PageRequest pageRequest = PageRequest.of(page, resolvedSize, Sort.by(direction, sort));

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Lista de pedidos",
                        PageResponse.from(service.listar(pageRequest))
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substitui completamente um pedido ainda editavel")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody PedidoUpdateDTO dto){
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido atualziado", service.atualizar(id, dto)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza parcialmente um pedido ainda editavel")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> atualizarParcial(
            @PathVariable UUID id,
            @Valid @RequestBody PedidoPatchDTO dto
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido atualizado parcialmente", service.atualizarParcial(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um pedido quando permitido")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pedido cancelado", null));
    }
}
