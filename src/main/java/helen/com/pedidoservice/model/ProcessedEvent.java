package helen.com.pedidoservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_event")
@IdClass(ProcessedEvent.ProcessedEventId.class)
@Getter
@Setter
public class ProcessedEvent {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Id
    @Column(name = "consumer", nullable = false, length = 120)
    private String consumer;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessedEventId implements Serializable {
        private UUID eventId;
        private String consumer;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProcessedEventId that)) {
                return false;
            }
            return Objects.equals(eventId, that.eventId) && Objects.equals(consumer, that.consumer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, consumer);
        }
    }
}
