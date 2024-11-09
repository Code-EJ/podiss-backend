// JwtRequestFilter.java
package com.code.yolanda.back.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.code.yolanda.back.user.UserService;
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

        // Confirmando se o token contem no cabeçalho o "Bearer" antes de começar o codigo
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                // Token inválido
                logger.error("Token JWT inválido", e);
            }
        } else {
            logger.warn("JWT Token não encontrado ou não começa com 'Bearer '");
        }

        // apenas validando o token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userService.loadUserByUsername(username);

            // Se o token for válido boto autenticado no usuario
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                        .setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request));
                // Depois de definir a autenticação no contexto, especificamos
                // que o usuário está autenticado e passa pelas configurações de segurança
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
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

        // Rotas públicas: GET /posts/** e GET /episodes/** coloquei tambem como outras opçoes o h2-console para teste e os videos.
        if (HttpMethod.GET.matches(method)) {
            if (pathMatcher.match("/posts/**", uri) || pathMatcher.match("/episodes/**", uri) || (pathMatcher.match("/video/**", uri) || (pathMatcher.match("/h2-console/**", uri)))){
                return true;
            }
        }

        return false;
    }
}
