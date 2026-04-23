package helen.com.pedidoservice.dto;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
        UUID eventId,
        String correlationId,
        String traceId,
        int version,
        Instant occurredAt
) {
    public static EventMetadata create(String correlationId, String traceId) {
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
        String resolvedTraceId = traceId == null || traceId.isBlank()
                ? resolvedCorrelationId
                : traceId;

        return new EventMetadata(
                UUID.randomUUID(),
                resolvedCorrelationId,
                resolvedTraceId,
                1,
                Instant.now()
        );
    }
}
