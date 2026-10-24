package com.infrapilot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Pipeline callback endpoints (allow pipeline token or devops/admin)
                .requestMatchers("/api/v1/callbacks/**").hasAnyRole("PIPELINE", "DEVOPS", "ADMIN")

                // Approval & destruction gates limited to DEVOPS and ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/runs/*/approve").hasAnyRole("DEVOPS", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/runs/*/reject").hasAnyRole("DEVOPS", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/runs/*/destroy").hasAnyRole("DEVOPS", "ADMIN")

                // Admin-only endpoints
                .requestMatchers("/api/v1/audit/**").hasRole("ADMIN")

                // Creation endpoints (Developer+)
                .requestMatchers(HttpMethod.POST, "/api/v1/projects").hasAnyRole("DEVELOPER", "DEVOPS", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/runs").hasAnyRole("DEVELOPER", "DEVOPS", "ADMIN")

                // Read endpoints (Authenticated: VIEWER, DEVELOPER, DEVOPS, ADMIN)
                .requestMatchers("/api/v1/**").authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
