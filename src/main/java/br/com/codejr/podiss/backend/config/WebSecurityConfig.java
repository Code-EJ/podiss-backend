// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.config;
import br.com.codejr.podiss.backend.common.ProblemResponseWriter;
import br.com.codejr.podiss.backend.security.*;
import br.com.codejr.podiss.backend.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration @EnableMethodSecurity
public class WebSecurityConfig {
    @Bean
    AuthenticationManager authenticationManager(UserService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("X-Total-Count", "X-Total-Pages", "X-Page", "X-Page-Size", "Retry-After"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwt,
            SubmissionRateLimitFilter limit, JwtAuthenticationEntryPoint entry, ObjectMapper mapper,
            @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> c.configurationSource(cors))
            .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(c -> c
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/contatos", "/sugestoes").permitAll()
                .requestMatchers(HttpMethod.GET, "/posts", "/posts/**", "/episodes", "/episodes/**").permitAll()
                .anyRequest().hasRole("ADMIN"))
            .exceptionHandling(c -> c.authenticationEntryPoint(entry)
                .accessDeniedHandler((request, response, exception) ->
                    ProblemResponseWriter.write(response, mapper, 403, "Esta operação exige acesso de administrador.")))
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(limit, JwtRequestFilter.class).build();
    }
    @Bean
    FilterRegistrationBean<JwtRequestFilter> jwtRegistration(JwtRequestFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }
    @Bean
    FilterRegistrationBean<SubmissionRateLimitFilter> limitRegistration(SubmissionRateLimitFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }
}
