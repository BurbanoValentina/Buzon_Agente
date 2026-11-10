package com.ucc.complaints.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final AdminSessionAuthenticationFilter adminSessionAuthenticationFilter;

    public SecurityConfig(AdminSessionAuthenticationFilter adminSessionAuthenticationFilter) {
        this.adminSessionAuthenticationFilter = adminSessionAuthenticationFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**", "/api/v1/openapi/**", "/api/v1/swagger-ui/**",
                    "/actuator/health").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(session -> session
                .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::none))
            .addFilterBefore(adminSessionAuthenticationFilter, AnonymousAuthenticationFilter.class)
            .build();
    }
}
