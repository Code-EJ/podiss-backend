// Credits: oEnzoRibas
package br.com.codejr.podiss.backend.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login response containing a Bearer token. Consumers must treat the value as a secret and never log it.
 *
 * @author oEnzoRibas
 */
@Data
@AllArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(description = "Bearer token; treat as a secret.")
public class JwtResponse {
    private String token;
}
