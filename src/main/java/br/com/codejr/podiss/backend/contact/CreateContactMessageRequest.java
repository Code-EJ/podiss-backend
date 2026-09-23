// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.contact;
import jakarta.validation.constraints.*;
/**
 * Public contact payload. Portuguese component names are part of the external JSON contract, not physical column names.
 *
 * @author oEnzoRibas
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "Portuguese JSON fields are intentional legacy contract names.")
public record CreateContactMessageRequest(
    @NotBlank @Size(max = 255) String nome,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 255) String assunto,
    @NotBlank @Size(max = 10000) String mensagem) {}
