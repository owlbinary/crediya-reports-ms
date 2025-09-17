package com.crediya.api.security;

import lombok.Builder;
import lombok.Data;

import java.security.Principal;

/**
 * Principal personalizado que contiene información del usuario autenticado vía JWT.
 */
@Data
@Builder
public class JwtUserPrincipal implements Principal {
    
    private final String email;
    private final String idUsuario;
    private final String nombre;
    private final String apellido;
    private final String idRol;
    
    @Override
    public String getName() {
        return email;
    }
}
