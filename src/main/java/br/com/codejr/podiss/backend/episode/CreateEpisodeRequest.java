// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.episode;
import jakarta.validation.constraints.*;
public record CreateEpisodeRequest(@NotBlank @Size(max = 2048) String videoUrl,
                              @Size(max = 10000) String description) {}
