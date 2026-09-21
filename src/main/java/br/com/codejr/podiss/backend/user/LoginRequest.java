// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.validation.constraints.*;
/**
 * Login credentials accepted by the public authentication endpoint. Transport must be protected by HTTPS in production.
 *
 * @author oEnzoRibas
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "Credentials; never log the password.")
public record LoginRequest(@NotBlank @Size(max = 100) String username,
                           @io.swagger.v3.oas.annotations.media.Schema(accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY, format = "password")
                           @NotBlank @Size(max = 72) String password) {}
