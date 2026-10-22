package com.backend.form_to_sheets_api.security;

import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Opcional: Aplicar el rate limit únicamente al endpoint del formulario para no bloquear recursos estáticos o de salud
        String path = request.getRequestURI();
        if (path.contains("/api/forms") || path.contains("/api/v1/forms")) { // Ajusta según la ruta de tu controlador
            String clientIp = getClientIP(request);
            Bucket bucket = rateLimitService.resolveBucket(clientIp);

            // Intentamos consumir 1 ficha
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response); // ¡Pasa la petición!
            } else {
                // Si no hay fichas, respondemos con 429 Too Many Requests
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"You have exceeded your 24-hour request limit. Please try again tomorrow.\"}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
    // Método para obtener la dirección ip
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}