package com.sign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sign.security.JwtAuthFilter;

import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())    
            .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/swagger-ui/**",   // Swagger UI
                            "/swagger-ui.html", // Página principal do Swagger
                            "/v3/api-docs/**",  // OpenAPI JSON/YAML
                            "/api-docs/**",      // Documentação da API
                            "/api/pdf/validation",
                            "/api/test**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        // Configuração de CORS restrita para /api/certificates/**
        CorsConfiguration certificatesConfig = new CorsConfiguration();
        certificatesConfig.setAllowedOrigins(List.of("http://localhost"));  // Permitir apenas localhost
        certificatesConfig.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        certificatesConfig.setAllowedHeaders(List.of("*"));
        certificatesConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/certificates/**", certificatesConfig);

        return source;
    }
}

