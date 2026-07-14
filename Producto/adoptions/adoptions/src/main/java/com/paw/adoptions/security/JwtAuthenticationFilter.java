package com.paw.adoptions.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Value("${paw.security.forwarded-headers-enabled:false}")
    private boolean forwardedHeadersEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            AuthenticatedUser user = resolveUser(request);
            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authority = new SimpleGrantedAuthority("ROLE_" + user.role().name());
                var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser resolveUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return jwtService.parseToken(authorization.substring(7));
        }

        if (!forwardedHeadersEnabled) {
            return null;
        }

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");
        if (userId == null || role == null) {
            return null;
        }

        return new AuthenticatedUser(UUID.fromString(userId), email, UserRole.from(role));
    }
}
