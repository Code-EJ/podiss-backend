// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.common;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;

/**
 * Translates expected domain, validation and persistence failures into ProblemDetail responses without database internals.
 *
 * @author oEnzoRibas
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> domain(ApiException e) { return problem(e.getStatus(), e.getMessage()); }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ProblemDetail> validation(BindException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage()).distinct()
            .collect(java.util.stream.Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class})
    ResponseEntity<ProblemDetail> invalid(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "Dados inválidos. Confira os campos e identificadores.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> conflict(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "Conflito de dados: o registro já existe ou viola uma restrição.");
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> authentication(AuthenticationException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Usuário, senha ou token inválidos.");
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> forbidden(AccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, "Esta operação exige acesso de administrador.");
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ProblemDetail> upload(MaxUploadSizeExceededException e) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "A imagem deve ter no máximo 5 MB.");
    }
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
