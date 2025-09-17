package com.crediya.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

/**
 * Utilidad para obtener información del usuario autenticado.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Obtiene el principal del usuario autenticado.
     *
     * @return Mono que emite el JwtUserPrincipal del usuario autenticado
     */
    public static Mono<JwtUserPrincipal> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(Authentication.class)
                .map(authentication -> (JwtUserPrincipal) authentication.getPrincipal());
    }

    /**
     * Obtiene el ID del usuario autenticado.
     *
     * @return Mono que emite el ID del usuario autenticado
     */
    public static Mono<String> getCurrentUserId() {
        return getCurrentUser()
                .map(JwtUserPrincipal::getIdUsuario);
    }

    /**
     * Obtiene el email del usuario autenticado.
     *
     * @return Mono que emite el email del usuario autenticado
     */
    public static Mono<String> getCurrentUserEmail() {
        return getCurrentUser()
                .map(JwtUserPrincipal::getEmail);
    }
}
