// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import jakarta.validation.constraints.*;
/**
 * Episode submission referencing a YouTube URL. An optional description overrides provider metadata; no arbitrary URL is fetched.
 *
 * @author oEnzoRibas
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "YouTube URL with optional description override.")
public record CreateEpisodeRequest(@NotBlank @Size(max = 2048) String videoUrl,
                              @Size(max = 10000) String description) {}
