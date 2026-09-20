// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.security;
import br.com.codejr.podiss.backend.common.ProblemResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.*;
import java.util.*;
@Component
public class SubmissionRateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new HashMap<>();
    private final ObjectMapper mapper;
    private final int limit;
    private final long duration;
    public SubmissionRateLimitFilter(ObjectMapper mapper, @Value("${app.submissions.limit:10}") int limit,
                                     @Value("${app.submissions.window:PT1M}") Duration duration) {
        if (limit < 1 || duration.toMillis() < 1) throw new IllegalArgumentException("Limite e janela devem ser positivos.");
        this.mapper = mapper; this.limit = limit; this.duration = duration.toMillis();
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) ||
            !Set.of("/contatos", "/sugestoes").contains(request.getServletPath());
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long retry = reserve(request.getRemoteAddr(), System.currentTimeMillis());
        if (retry > 0) {
            response.setHeader("Retry-After", Long.toString(retry));
            ProblemResponseWriter.write(response, mapper, 429, "Muitas mensagens. Tente novamente mais tarde.");
            return;
        }
        chain.doFilter(request, response);
    }
    private synchronized long reserve(String ip, long now) {
        windows.entrySet().removeIf(e -> e.getValue().until <= now);
        Window window = windows.get(ip);
        if (window == null) {
            if (windows.size() >= 10000) return Math.max(1, duration / 1000);
            window = new Window(now + duration);
            windows.put(ip, window);
        }
        if (window.count >= limit) return Math.max(1, (window.until - now + 999) / 1000);
        window.count++;
        return 0;
    }
    private static class Window {
        final long until; int count;
        Window(long until) { this.until = until; }
    }
}
