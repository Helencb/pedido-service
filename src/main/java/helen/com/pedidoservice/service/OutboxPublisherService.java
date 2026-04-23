package helen.com.pedidoservice.service;

import helen.com.pedidoservice.config.RabbitConfig;
import helen.com.pedidoservice.dto.PedidoEvent;
import helen.com.pedidoservice.model.OutboxEvent;
import helen.com.pedidoservice.model.OutboxStatus;
import helen.com.pedidoservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {
    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.outbox.auto-publish:true}")
    private boolean autoPublish;

    public void store(PedidoEvent event, String routingKey) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(event.metadata().eventId());
        outboxEvent.setAggregateId(event.pedidoId());
        outboxEvent.setEventType(event.getClass().getName());
        outboxEvent.setRoutingKey(routingKey);
        outboxEvent.setPayload(writePayload(event));
        outboxEvent.setCorrelationId(event.metadata().correlationId());
        outboxEvent.setTraceId(event.metadata().traceId());
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setAttempts(0);

        outboxEventRepository.save(outboxEvent);

        if (autoPublish) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publishById(outboxEvent.getId());
                    }
                });
            } else {
                publishById(outboxEvent.getId());
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.retry-delay-ms:30000}")
    public void retryPending() {
        if (!autoPublish) {
            return;
        }

        List<OutboxEvent> pendentes = outboxEventRepository.findTop50ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        );

        pendentes.forEach(event -> publishById(event.getId()));
    }

    public void publishById(UUID id) {
        OutboxEvent outboxEvent = outboxEventRepository.findById(id).orElse(null);
        if (outboxEvent == null || outboxEvent.getStatus() == OutboxStatus.PUBLISHED) {
            return;
        }

        try {
            Class<?> eventType = Class.forName(outboxEvent.getEventType());
            Object payload = objectMapper.readValue(outboxEvent.getPayload(), eventType);
            CorrelationData correlationData = new CorrelationData(outboxEvent.getId().toString());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    outboxEvent.getRoutingKey(),
                    payload,
                    message -> {
                        message.getMessageProperties().setHeader("eventId", outboxEvent.getId().toString());
                        message.getMessageProperties().setHeader("correlationId", outboxEvent.getCorrelationId());
                        message.getMessageProperties().setHeader("traceId", outboxEvent.getTraceId());
                        message.getMessageProperties().setHeader("eventType", outboxEvent.getEventType());
                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException("Broker retornou nack para o evento " + outboxEvent.getId());
            }

            outboxEvent.setStatus(OutboxStatus.PUBLISHED);
            outboxEvent.setPublishedAt(Instant.now());
            outboxEvent.setLastError(null);
            outboxEventRepository.save(outboxEvent);

            log.info("Evento {} publicado em {} correlationId={}",
                    outboxEvent.getId(), outboxEvent.getRoutingKey(), outboxEvent.getCorrelationId());
        } catch (Exception ex) {
            outboxEvent.setStatus(OutboxStatus.FAILED);
            outboxEvent.setAttempts(outboxEvent.getAttempts() + 1);
            outboxEvent.setLastError(ex.getMessage());
            outboxEventRepository.save(outboxEvent);
            log.error("Falha ao publicar evento {} em {} correlationId={}",
                    outboxEvent.getId(), outboxEvent.getRoutingKey(), outboxEvent.getCorrelationId(), ex);
        }
    }

    private String writePayload(PedidoEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao serializar evento", ex);
        }
    }
}
