package com.crediya.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Servicio para manejo de tokens JWT.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Valida un token JWT.
     *
     * @param token Token a validar
     * @return true si el token es válido
     */
    public boolean validarToken(String token) {
        try {
            Claims claims = extraerClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el email del usuario del token.
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    /**
     * Extrae el ID del usuario del token.
     *
     * @param token Token JWT
     * @return ID del usuario
     */
    public String extraerIdUsuario(String token) {
        Claims claims = extraerClaims(token);
        return extraerClaimComoString(claims, "userId");
    }

    /**
     * Extrae el nombre del usuario del token.
     *
     * @param token Token JWT
     * @return Nombre del usuario
     */
    public String extraerNombre(String token) {
        Claims claims = extraerClaims(token);
        return extraerClaimComoString(claims, "nombre");
    }

    /**
     * Extrae el apellido del usuario del token.
     *
     * @param token Token JWT
     * @return Apellido del usuario
     */
    public String extraerApellido(String token) {
        Claims claims = extraerClaims(token);
        return extraerClaimComoString(claims, "apellido");
    }

    /**
     * Extrae el ID del rol del usuario del token.
     *
     * @param token Token JWT
     * @return ID del rol del usuario
     */
    public String extraerIdRol(String token) {
        Claims claims = extraerClaims(token);
        return extraerClaimComoString(claims, "idRol");
    }

    /**
     * Extrae las autoridades del usuario basándose en el token.
     *
     * @param token Token JWT
     * @return Colección de autoridades
     */
    public Collection<GrantedAuthority> extraerAutoridades(String token) {
        String idRol = extraerIdRol(token);
        String autoridad = "ROLE_" + mapearRol(idRol);
        return List.of(new SimpleGrantedAuthority(autoridad));
    }

    /**
     * Mapea el ID del rol a su nombre correspondiente.
     */
    private String mapearRol(String idRol) {
        if (idRol == null) return "USER";
        
        return switch (idRol) {
            case "1" -> "ADMIN";
            case "2" -> "ASESOR";
            case "3" -> "CLIENTE";
            default -> "USER";
        };
    }

    /**
     * Extrae todos los claims del token.
     *
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae un claim y lo convierte a String, manejando diferentes tipos de datos.
     *
     * @param claims Claims del token
     * @param claimName Nombre del claim
     * @return Valor del claim como String
     */
    private String extraerClaimComoString(Claims claims, String claimName) {
        Object claimValue = claims.get(claimName);
        return claimValue != null ? claimValue.toString() : null;
    }
}
