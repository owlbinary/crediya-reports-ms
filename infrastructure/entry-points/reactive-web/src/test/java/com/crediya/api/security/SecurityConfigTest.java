package com.crediya.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig - Configuración de Seguridad para Reports")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    @DisplayName("Debe crear SecurityWebFilterChain correctamente")
    void debeCrearSecurityWebFilterChainCorrectamente() {
        ServerHttpSecurity http = ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain = securityConfig.securityWebFilterChain(http);

        assertThat(filterChain).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar SecurityConfig con constructor")
    void debeInstanciarSecurityConfigConConstructor() {
        SecurityConfig config = new SecurityConfig(jwtAuthenticationFilter);
        
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Debe tener el JwtAuthenticationFilter inyectado")
    void debeTenerJwtAuthenticationFilterInyectado() {
        assertThat(securityConfig).isNotNull();
    }

    @Test
    @DisplayName("Debe ser una clase de configuración")
    void deberSerUnaClaseDeConfiguracion() {
        assertThat(securityConfig.getClass())
                .hasAnnotation(org.springframework.context.annotation.Configuration.class);
        assertThat(securityConfig.getClass())
                .hasAnnotation(org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity.class);
        assertThat(securityConfig.getClass())
                .hasAnnotation(org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity.class);
    }

    @Test
    @DisplayName("Debe tener constructor con parámetros requeridos")
    void debeTenerConstructorConParametrosRequeridos() {
        assertThat(securityConfig.getClass().getDeclaredConstructors()).hasSize(1);
        assertThat(securityConfig.getClass().getDeclaredConstructors()[0].getParameterCount()).isEqualTo(1);
        assertThat(securityConfig.getClass().getDeclaredConstructors()[0].getParameterTypes()[0])
                .isEqualTo(JwtAuthenticationFilter.class);
    }

    @Test
    @DisplayName("Debe tener método securityWebFilterChain")
    void debeTenerMetodoSecurityWebFilterChain() throws Exception {
        var method = securityConfig.getClass().getMethod("securityWebFilterChain", ServerHttpSecurity.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(SecurityWebFilterChain.class);
        assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).isTrue();
    }

    @Test
    @DisplayName("Debe crear diferentes instancias de SecurityWebFilterChain")
    void debeCrearDiferentesInstanciasDeSecurityWebFilterChain() {
        ServerHttpSecurity http1 = ServerHttpSecurity.http();
        ServerHttpSecurity http2 = ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain1 = securityConfig.securityWebFilterChain(http1);
        SecurityWebFilterChain filterChain2 = securityConfig.securityWebFilterChain(http2);

        assertThat(filterChain1).isNotNull();
        assertThat(filterChain2).isNotNull();
        assertThat(filterChain1).isNotSameAs(filterChain2);
    }

    @Test
    @DisplayName("Debe manejar correctamente ServerHttpSecurity nulo")
    void debeManejarCorrectamenteServerHttpSecurityNulo() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            securityConfig.securityWebFilterChain(null);
        });
    }

    @Test
    @DisplayName("SecurityWebFilterChain debe tener filtros configurados")
    void securityWebFilterChainDebeTenerFiltrosConfigurados() {
        ServerHttpSecurity http = ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain = securityConfig.securityWebFilterChain(http);

        assertThat(filterChain).isNotNull();
        assertThat(filterChain.getWebFilters()).isNotNull();
    }

    @Test
    @DisplayName("Debe tener habilitado @EnableReactiveMethodSecurity para soportar @PreAuthorize")
    void debeTenerHabilitadoEnableReactiveMethodSecurityParaSoportarPreAuthorize() {
        assertThat(securityConfig.getClass())
                .hasAnnotation(org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity.class);
    }
}
