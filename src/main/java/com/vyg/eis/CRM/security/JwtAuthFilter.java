package com.vyg.eis.CRM.security;

import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import com.vyg.eis.CRM.service.CRM.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extractAllClaims(token);
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            String name = claims.get("name", String.class);

            UserPrincipal principal = new UserPrincipal(userId, email, role, name);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority(role.name()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            log.warn("JWT auth failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
