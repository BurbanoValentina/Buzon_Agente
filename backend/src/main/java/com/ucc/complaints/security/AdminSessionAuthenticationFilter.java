package com.ucc.complaints.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminSessionAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectProvider<AdminAuthenticationService> authenticationServiceProvider;

    public AdminSessionAuthenticationFilter(ObjectProvider<AdminAuthenticationService> authenticationServiceProvider) {
        this.authenticationServiceProvider = authenticationServiceProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String username = null;

        AdminAuthenticationService authService = authenticationServiceProvider.getIfAvailable();

        // 1. Validar por Header Authorization: Bearer <token> o X-Admin-Token
        if (authService != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                username = authService.getUsernameForToken(token);
            }
            if (username == null) {
                String customToken = request.getHeader("X-Admin-Token");
                if (customToken != null && !customToken.isBlank()) {
                    username = authService.getUsernameForToken(customToken.trim());
                }
            }
        }

        // 2. Validar por Session Cookie (fallback)
        if (username == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object attr = session.getAttribute(AdminAuthenticationService.SESSION_USERNAME);
                if (attr instanceof String s && !s.isBlank()) {
                    username = s;
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
