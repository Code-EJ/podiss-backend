// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank @Size(max = 100) String username,
                           @NotBlank @Size(max = 72) String password) {}
