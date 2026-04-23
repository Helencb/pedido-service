package helen.com.pedidoservice.service;

import helen.com.pedidoservice.dto.PedidoEvent;
import helen.com.pedidoservice.model.ProcessedEvent;
import helen.com.pedidoservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProcessingService {
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void executeIfNew(PedidoEvent event, String consumer, Runnable action) {
        if (processedEventRepository.existsByEventIdAndConsumer(event.metadata().eventId(), consumer)) {
            log.info("Evento {} ja processado pelo consumer {}", event.metadata().eventId(), consumer);
            return;
        }

        action.run();

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.metadata().eventId());
        processedEvent.setConsumer(consumer);
        processedEvent.setProcessedAt(Instant.now());
        processedEventRepository.save(processedEvent);
    }
}
