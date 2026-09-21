// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.common;
import lombok.Getter;
import org.springframework.http.HttpStatus;


/**
 * Domain failure with an intentional HTTP status and a client-safe message. Never include credentials or SQL in its detail.
 *
 * @author oEnzoRibas
 */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status, String message) { super(message); this.status = status; }
}
