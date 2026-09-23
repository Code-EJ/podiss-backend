// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;
import java.util.UUID;
/**
 * Editorial post with optional image bytes stored separately from list projections. Tags persist as a comma-separated string.
 *
 * @author oEnzoRibas
 */
@Entity @Table(name = "posts") @Getter @Setter @NoArgsConstructor
public class Post {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) @Column(name = "id")
    private UUID id;
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "tags", nullable = false, length = 2048) private String tags = "";
    @Column(name = "created_at", nullable = false, updatable = false) private Timestamp createdAt;
    @Lob @Column(name = "image", columnDefinition = "LONGBLOB") private byte[] image;
    @Column(name = "image_content_type", length = 100) private String imageContentType;
}
