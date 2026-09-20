// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.contact;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;
import java.util.UUID;
@Entity(name = "Contato") @Table(name = "contact_messages") @Getter @Setter @NoArgsConstructor
public class ContactMessage {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) @Column(name = "id")
    private UUID id;
    @JsonProperty("nome") @Column(name = "sender_name", nullable = false, length = 255) private String senderName;
    @Column(name = "email", nullable = false, length = 255) private String email;
    @JsonProperty("assunto") @Column(name = "subject", nullable = false, length = 255) private String subject;
    @JsonProperty("mensagem") @Column(name = "message", nullable = false, columnDefinition = "TEXT") private String message;
    @Column(name = "created_at", nullable = false, updatable = false) private Timestamp createdAt;
}
