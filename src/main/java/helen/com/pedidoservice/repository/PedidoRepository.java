package helen.com.pedidoservice.repository;

import helen.com.pedidoservice.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    Page<Pedido> findByAtivoTrue(Pageable pageable);

    Optional<Pedido> findByIdAndAtivoTrue(UUID id);
}
