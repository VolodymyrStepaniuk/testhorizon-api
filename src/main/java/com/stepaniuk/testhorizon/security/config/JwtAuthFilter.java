package com.stepaniuk.testhorizon.security.config;

import com.stepaniuk.testhorizon.security.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        boolean isAuthSuccessful = true;

        final String authHeader = request.getHeader("Authorization");

        if (isInvalidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = extractJwtFromHeader(authHeader);
            final String userEmail = jwtProvider.extractUsername(jwt);

            if (userEmail != null && isAuthenticationAbsent()) {
                authenticateUser(request, jwt, userEmail);
            }
        } catch (ExpiredJwtException | MalformedJwtException e) {
            isAuthSuccessful = false;

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.getWriter().flush();
        }

        if (isAuthSuccessful) {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isInvalidAuthHeader(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private String extractJwtFromHeader(String authHeader) {
        return authHeader.substring(7);
    }

    private boolean isAuthenticationAbsent() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void authenticateUser(HttpServletRequest request, String jwt, String userEmail) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (jwtProvider.isTokenValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
