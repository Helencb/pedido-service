package helen.com.pedidoservice.repository;

import helen.com.pedidoservice.model.OutboxEvent;
import helen.com.pedidoservice.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByStatusInOrderByCreatedAtAsc(List<OutboxStatus> statuses);
}
