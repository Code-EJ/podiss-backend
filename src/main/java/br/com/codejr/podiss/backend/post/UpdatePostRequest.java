// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import jakarta.validation.constraints.*;
import java.util.List;
/**
 * Partial editorial update carried by PUT JSON. Null fields preserve existing values; image changes use dedicated endpoints.
 *
 * @author oEnzoRibas
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "Null fields preserve existing values. Empty tags list clears tags.")
public record UpdatePostRequest(
    @Size(min = 1, max = 255) @Pattern(regexp = "(?s).*\\S.*") String title,
    @Size(min = 1, max = 10000) @Pattern(regexp = "(?s).*\\S.*") String description,
    @Size(max = 20) List<@NotBlank @Size(max = 80) String> tags) {}
