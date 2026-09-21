// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
/**
 * Multipart post creation fields. Tags are submitted as a list and normalized by the service; the image is optional.
 *
 * @author oEnzoRibas
 */
@Data
public class CreatePostRequest {
    @NotBlank @Size(max = 255) private String title;
    @NotBlank @Size(max = 10000) private String description;
    @Size(max = 20) private List<@NotBlank @Size(max = 80) String> tags;
    private MultipartFile image;
}
