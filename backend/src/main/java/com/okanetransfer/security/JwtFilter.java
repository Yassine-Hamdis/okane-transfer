package com.okanetransfer.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private static final List<String> WHITELIST = List.of(
            "/api/auth/", "/v3/api-docs", "/v3/api-docs/",
            "/swagger-ui", "/swagger-ui/", "/swagger-ui/index.html",
            "/swagger-ui.html", "/webjars/"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info(">>> JWT FILTER EXECUTED FOR URI: {}", request.getRequestURI());

        String context = request.getContextPath() == null ? "" : request.getContextPath();
        String path = request.getRequestURI().substring(context.length());

        // 1. Skip filter for whitelisted paths
        for (String prefix : WHITELIST) {
            if (path.startsWith(prefix)) {
                log.info(">>> Path {} is whitelisted, skipping JWT check.", path);
                filterChain.doFilter(request, response);
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");
        log.info(">>> Auth Header found: {}", authHeader != null ? "YES" : "NO");

        // 2. If no valid header, continue chain (SecurityConfig will block it if unauthorized)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(">>> Request rejected by filter: Header is null or missing 'Bearer '");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info(">>> Token extracted successfully.");

        // 3. ONLY wrap the JWT parsing in the try-catch block
        try {
            if (jwtUtil.isTokenValid(token)) {
                log.info(">>> Is Token Valid? {}", jwtUtil.isTokenValid(token));
                String email = jwtUtil.extractEmail(token);
                log.info(">>> Extracted Email: {}", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    log.info(">>> User loaded from DB: {}", userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info(">>> Security Context set with Authorities: {}", userDetails.getAuthorities());
                }
            }else {
                log.warn(">>> Token validation returned false!");
            }
        } catch (Exception ex) {
            log.error("JWT processing failed for request {}: {}", path, ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired JWT token\"}");
            return; // Stop here if the token is actively malformed/expired
        }

        // 4. Call doFilter OUTSIDE the try-catch block!
        filterChain.doFilter(request, response);
    }
}