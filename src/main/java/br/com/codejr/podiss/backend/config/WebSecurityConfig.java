package br.com.codejr.podiss.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.codejr.podiss.backend.security.JwtAuthenticationEntryPoint;
import br.com.codejr.podiss.backend.security.JwtRequestFilter;
import br.com.codejr.podiss.backend.user.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private JwtRequestFilter authFilter;

    @Autowired
    private UserService userService;

    @Bean
    AuthenticationManager authenticationManagerBean(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return builder.build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/episodes/**", "/video/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(unauthorizedHandler)
            )
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
