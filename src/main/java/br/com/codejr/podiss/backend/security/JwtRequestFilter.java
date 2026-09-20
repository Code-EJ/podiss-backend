// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.security;
import br.com.codejr.podiss.backend.user.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component @RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final UserService users;
    private final JwtTokenService tokens;
    private final JwtAuthenticationEntryPoint entryPoint;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer ") || header.length() == 7) throw new BadCredentialsException("Bearer inválido.");
                var user = users.loadUserByUsername(tokens.getUsernameFromToken(header.substring(7)));
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException | BadCredentialsException e) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, new BadCredentialsException("Token inválido.", e));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
