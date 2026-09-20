// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.contact;
import jakarta.validation.constraints.*;
public record CreateContactMessageRequest(
    @NotBlank @Size(max = 255) String nome,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 255) String assunto,
    @NotBlank @Size(max = 10000) String mensagem) {}
