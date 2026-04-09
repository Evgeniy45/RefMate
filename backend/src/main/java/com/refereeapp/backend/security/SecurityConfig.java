package com.refereeapp.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*", "Authorization")); 
                return config;
            }))
            // Кажемо Spring Security НЕ створювати сесії (бо в нас REST API)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ДОЗВОЛЕНО ВСІМ: логін, реєстрація ТА сторінки документації Swagger
                .requestMatchers(
                        "/api/users/login", 
                        "/api/users/register",
                        "/v3/api-docs/**", 
                        "/swagger-ui/**", 
                        "/swagger-ui.html"
                ).permitAll()
                // УСЕ ІНШЕ: тільки з токеном
                .anyRequest().authenticated()
            )
            // Додаємо нашого фільтра перед стандартним фільтром Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}