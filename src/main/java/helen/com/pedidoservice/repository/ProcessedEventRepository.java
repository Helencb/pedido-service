package helen.com.pedidoservice.repository;

import helen.com.pedidoservice.model.ProcessedEvent;
import helen.com.pedidoservice.model.ProcessedEvent.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {
    boolean existsByEventIdAndConsumer(UUID eventId, String consumer);
}
