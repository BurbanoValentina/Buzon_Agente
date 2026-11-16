package com.ucc.complaints.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminSessionAuthenticationFilter extends OncePerRequestFilter {

    private final AdminAuthenticationService authenticationService;

    public AdminSessionAuthenticationFilter(AdminAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String username = null;

        // 1. Validar por Header Authorization: Bearer <token> o X-Admin-Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            username = authenticationService.getUsernameForToken(token);
        }
        if (username == null) {
            String customToken = request.getHeader("X-Admin-Token");
            if (customToken != null && !customToken.isBlank()) {
                username = authenticationService.getUsernameForToken(customToken.trim());
            }
        }

        // 2. Fallback: validar por HttpSession / Cookie JSESSIONID
        if (username == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object sessionUser = session.getAttribute(AdminAuthenticationService.SESSION_USERNAME);
                if (sessionUser instanceof String val && !val.isBlank()) {
                    username = val;
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                username, null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
