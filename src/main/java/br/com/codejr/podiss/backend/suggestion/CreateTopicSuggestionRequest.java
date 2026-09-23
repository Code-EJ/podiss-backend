// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.suggestion;
import jakarta.validation.constraints.*;
/**
 * Public suggestion payload. Names nome and tema are intentionally retained as external API fields.
 *
 * @author oEnzoRibas
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "Portuguese JSON fields nome and tema are intentional legacy contract names.")
public record CreateTopicSuggestionRequest(
    @NotBlank @Size(max = 255) String nome,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 2000) String tema) {}
