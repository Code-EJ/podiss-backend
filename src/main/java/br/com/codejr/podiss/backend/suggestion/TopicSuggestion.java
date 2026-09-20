// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.suggestion;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;
import java.util.UUID;
@Entity(name = "Sugestao") @Table(name = "topic_suggestions") @Getter @Setter @NoArgsConstructor
public class TopicSuggestion {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) @Column(name = "id")
    private UUID id;
    @JsonProperty("nome") @Column(name = "sender_name", nullable = false, length = 255) private String senderName;
    @Column(name = "email", nullable = false, length = 255) private String email;
    @JsonProperty("tema") @Column(name = "topic", nullable = false, length = 2000) private String topic;
    @Column(name = "created_at", nullable = false, updatable = false) private Timestamp createdAt;
}
