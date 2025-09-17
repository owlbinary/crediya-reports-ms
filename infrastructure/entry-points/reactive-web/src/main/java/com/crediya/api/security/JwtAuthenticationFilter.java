package com.crediya.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Filtro para autenticación JWT en peticiones HTTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extraerToken(exchange);
        
        if (token == null || !jwtService.validarToken(token)) {
            log.warn("Token JWT inválido o ausente para path: {}", path);
            return handleUnauthorized(exchange);
        }

        return authenticateAndContinue(exchange, chain, token);
    }

    /**
     * Verifica si la ruta es pública y no requiere autenticación.
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars");
    }

    /**
     * Extrae el token JWT del header Authorization.
     */
    private String extraerToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Autentica al usuario y continúa con la cadena de filtros.
     */
    private Mono<Void> authenticateAndContinue(ServerWebExchange exchange, WebFilterChain chain, String token) {
        try {
            String email = jwtService.extraerEmail(token);
            String idUsuario = jwtService.extraerIdUsuario(token);
            Collection<GrantedAuthority> authorities = jwtService.extraerAutoridades(token);

            JwtUserPrincipal principal = JwtUserPrincipal.builder()
                    .email(email)
                    .idUsuario(idUsuario)
                    .nombre(jwtService.extraerNombre(token))
                    .apellido(jwtService.extraerApellido(token))
                    .idRol(jwtService.extraerIdRol(token))
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            log.error("Error al procesar token JWT: {}", e.getMessage());
            return handleUnauthorized(exchange);
        }
    }

    /**
     * Maneja respuestas no autorizadas.
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
