// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import jakarta.validation.constraints.*;
/**
 * Episode submission referencing a YouTube URL. An optional description overrides provider metadata; no arbitrary URL is fetched.
 *
 * @author oEnzoRibas
 */
public record CreateEpisodeRequest(@NotBlank @Size(max = 2048) String videoUrl,
                              @Size(max = 10000) String description) {}
