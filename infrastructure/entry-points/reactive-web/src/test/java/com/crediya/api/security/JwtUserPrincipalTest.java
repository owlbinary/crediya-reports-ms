package com.crediya.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUserPrincipal - Principal de usuario JWT")
class JwtUserPrincipalTest {

    @Test
    @DisplayName("Debe crear principal con todos los datos")
    void debeCrearPrincipalConTodosLosDatos() {
        JwtUserPrincipal principal = JwtUserPrincipal.builder()
                .email("admin@ejemplo.com")
                .idUsuario("123")
                .nombre("Juan")
                .apellido("Pérez")
                .idRol("1")
                .build();

        assertThat(principal.getEmail()).isEqualTo("admin@ejemplo.com");
        assertThat(principal.getIdUsuario()).isEqualTo("123");
        assertThat(principal.getNombre()).isEqualTo("Juan");
        assertThat(principal.getApellido()).isEqualTo("Pérez");
        assertThat(principal.getIdRol()).isEqualTo("1");
        assertThat(principal.getName()).isEqualTo("admin@ejemplo.com");
    }

    @Test
    @DisplayName("Debe implementar equals y hashCode correctamente")
    void debeImplementarEqualsYHashCodeCorrectamente() {
        JwtUserPrincipal principal1 = JwtUserPrincipal.builder()
                .email("admin@ejemplo.com")
                .idUsuario("123")
                .nombre("Juan")
                .apellido("Pérez")
                .idRol("1")
                .build();

        JwtUserPrincipal principal2 = JwtUserPrincipal.builder()
                .email("admin@ejemplo.com")
                .idUsuario("123")
                .nombre("Juan")
                .apellido("Pérez")
                .idRol("1")
                .build();

        assertThat(principal1)
                .isEqualTo(principal2)
                .hasSameHashCodeAs(principal2);
    }

    @Test
    @DisplayName("Debe crear principal para usuario ADMIN")
    void debeCrearPrincipalParaUsuarioAdmin() {
        JwtUserPrincipal adminPrincipal = JwtUserPrincipal.builder()
                .email("admin@crediya.com")
                .idUsuario("1")
                .nombre("Admin")
                .apellido("Sistema")
                .idRol("1")
                .build();

        assertThat(adminPrincipal.getIdRol()).isEqualTo("1");
        assertThat(adminPrincipal.getEmail()).contains("admin");
        assertThat(adminPrincipal.getName()).isEqualTo("admin@crediya.com");
    }
}
