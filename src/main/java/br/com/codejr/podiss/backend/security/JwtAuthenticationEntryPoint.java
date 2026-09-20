// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.security;
import br.com.codejr.podiss.backend.common.ProblemResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component @RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        ProblemResponseWriter.write(response, mapper, 401, "Autenticação necessária ou token inválido/expirado.");
    }
}
