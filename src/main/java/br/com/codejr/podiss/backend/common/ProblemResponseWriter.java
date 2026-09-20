/**
 *
 *
 * @author oEnzoRibas ribas.enzo@juniorcode.com.br
 * @since 1.1.0
 */
package br.com.codejr.podiss.backend.common;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ProblemDetail;

public final class ProblemResponseWriter {
    private ProblemResponseWriter() {}
    public static void write(HttpServletResponse response, ObjectMapper mapper, int status, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        mapper.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(
            org.springframework.http.HttpStatusCode.valueOf(status), detail));
    }
}
