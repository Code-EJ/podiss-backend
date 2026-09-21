// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;
/**
 * Public post projection excluding image bytes. The derived image URL is relative to the API and absent when no image exists.
 *
 * @author oEnzoRibas
 */
public record PostResponse(UUID id, String title, String description, String tags,
                           Timestamp createdAt, boolean hasImage) {
    @JsonProperty public String imageUrl() { return hasImage ? "/posts/image/" + id : null; }
    static PostResponse from(Post p) {
        return new PostResponse(p.getId(), p.getTitle(), p.getDescription(), p.getTags(), p.getCreatedAt(),
            p.getImageContentType() != null);
    }
}
