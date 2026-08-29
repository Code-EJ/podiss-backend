package br.com.codejr.podiss.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import br.com.codejr.podiss.backend.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicRoute(requestURI, method)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // Confirmando se o token contem no cabeçalho o "Bearer" antes de começar o
        // codigo

        logger.debug(requestTokenHeader);

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Acesso negado. Você deve estar autenticado para acessar a URL solicitada.");
            return;
        }

        jwtToken = requestTokenHeader.substring(7);
        username = jwtTokenUtil.getUsernameFromToken(jwtToken);

        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Usuário não encontrado no token.");
                    return;
        }

        // apenas validando o token

        UserDetails userDetails = null;

        try {
            userDetails = this.userService.loadUserByUsername(username);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Usuário não encontrado.");
                    return;
        }

        if (!jwtTokenUtil.validateToken(jwtToken, userDetails)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Token inválido.");
                    return;
        }

        // Se o token for válido boto autenticado no usuario
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                        .buildDetails(request));
        // Depois de definir a autenticação no contexto, especificamos
        // que o usuário está autenticado e passa pelas configurações de segurança
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    
        chain.doFilter(request, response);
    }

    /**
     * Verifica se a rota é pública.
     *
     * @param uri    o URI da requisição
     * @param method o método HTTP da requisição
     * @return true se for rota pública, false caso contrário
     */
    private boolean isPublicRoute(String uri, String method) {
        // Rotas públicas: /api/auth/** para qualquer método
        if (pathMatcher.match("/api/auth/**", uri)) {
            return true;
        }

        // Rotas públicas: GET /posts/** e GET /episodes/** coloquei tambem como outras
        // opçoes o h2-console para teste e os videos.
        if (HttpMethod.GET.matches(method)) {
            return pathMatcher.match("/posts/**", uri) ||
                    pathMatcher.match("/episodes/**", uri) ||
                    pathMatcher.match("/video/**", uri) ||
                    pathMatcher.match("/h2-console/**", uri) ||
                    pathMatcher.match("/admin/login/**", uri);

        }

        return false;
    }
}
