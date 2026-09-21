// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;
import java.util.UUID;
/**
 * Published YouTube episode. Internal UUID identifies deletion; case-sensitive YouTube ID identifies public lookup.
 *
 * @author oEnzoRibas
 */
@Entity(name = "Video") @Table(name = "episodes") @Getter @Setter @NoArgsConstructor
public class Episode {
    @Id @GeneratedValue @JdbcTypeCode(SqlTypes.UUID) @Column(name = "id")
    private UUID id;
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "youtube_id", nullable = false, unique = true, length = 11) private String youtubeId;
    @Column(name = "video_url", nullable = false) private String videoUrl;
    @Column(name = "thumbnail_url", length = 512) private String thumbnailUrl;
    @Column(name = "created_at", nullable = false, updatable = false) private Timestamp createdAt;
}
