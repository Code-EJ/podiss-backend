// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.suggestion;
import jakarta.validation.constraints.*;
public record CreateTopicSuggestionRequest(
    @NotBlank @Size(max = 255) String nome,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 2000) String tema) {}
