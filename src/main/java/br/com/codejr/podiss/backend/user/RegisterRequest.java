// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.user;
import jakarta.validation.constraints.*;
/**
 * Administrator-controlled account creation. Role omission defaults to USER; password length also has a UTF-8 byte limit in the service.
 *
 * @author oEnzoRibas
 */
public record RegisterRequest(
    @NotBlank @Size(max = 100) @Pattern(regexp = "[a-zA-Z0-9._-]+") String username,
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Email @Size(max = 254) String email,
    User.Role role) {}
