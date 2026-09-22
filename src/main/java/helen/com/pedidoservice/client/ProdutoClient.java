package helen.com.pedidoservice.client;

import helen.com.pedidoservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProdutoClient {

    @GetMapping("/produtos/{id}")
    ApiResponse<ProdutoDTO> buscarPorId(@PathVariable("id") UUID id);
}
