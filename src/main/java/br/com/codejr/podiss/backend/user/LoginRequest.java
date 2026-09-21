// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.validation.constraints.*;
/**
 * Login credentials accepted by the public authentication endpoint. Transport must be protected by HTTPS in production.
 *
 * @author oEnzoRibas
 */
public record LoginRequest(@NotBlank @Size(max = 100) String username,
                           @NotBlank @Size(max = 72) String password) {}
