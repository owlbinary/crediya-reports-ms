package com.crediya.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtils - Utilidades de Seguridad para Reports")
class SecurityUtilsTest {

    private final JwtUserPrincipal testAdminPrincipal = JwtUserPrincipal.builder()
            .email("admin@crediya.com")
            .idUsuario("123")
            .nombre("Admin")
            .apellido("Sistema")
            .idRol("1")
            .build();

    @Test
    @DisplayName("Debe obtener el usuario ADMIN actual del contexto de seguridad")
    void debeObtenerUsuarioAdminActualDelContextoDeSeguridad() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(testAdminPrincipal, null);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        SecurityUtils.getCurrentUser()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .as(StepVerifier::create)
                .expectNext(testAdminPrincipal)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe obtener el ID del usuario ADMIN actual")
    void debeObtenerIdDelUsuarioAdminActual() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(testAdminPrincipal, null);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        SecurityUtils.getCurrentUserId()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .as(StepVerifier::create)
                .expectNext("123")
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe obtener el email del usuario ADMIN actual")
    void debeObtenerEmailDelUsuarioAdminActual() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(testAdminPrincipal, null);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        SecurityUtils.getCurrentUserEmail()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .as(StepVerifier::create)
                .expectNext("admin@crediya.com")
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe ser una clase final")
    void deberSerUnaClaseFinal() {
        assertThat(Modifier.isFinal(SecurityUtils.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Debe tener constructor privado")
    void debeTenerConstructorPrivado() throws Exception {
        Constructor<SecurityUtils> constructor = SecurityUtils.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Debe tener métodos estáticos")
    void debeTenerMetodosEstaticos() throws Exception {
        Method getCurrentUser = SecurityUtils.class.getMethod("getCurrentUser");
        Method getCurrentUserId = SecurityUtils.class.getMethod("getCurrentUserId");
        Method getCurrentUserEmail = SecurityUtils.class.getMethod("getCurrentUserEmail");

        assertThat(Modifier.isStatic(getCurrentUser.getModifiers())).isTrue();
        assertThat(Modifier.isStatic(getCurrentUserId.getModifiers())).isTrue();
        assertThat(Modifier.isStatic(getCurrentUserEmail.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Debe manejar contexto vacío correctamente")
    void debeManejarContextoVacioCorrectamente() {
        SecurityUtils.getCurrentUser()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe validar que el usuario obtenido es ADMIN")
    void debeValidarQueElUsuarioObtenidoEsAdmin() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(testAdminPrincipal, null);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        SecurityUtils.getCurrentUser()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .as(StepVerifier::create)
                .assertNext(user -> {
                    assertThat(user.getIdRol()).isEqualTo("1");
                    assertThat(user.getEmail()).contains("admin");
                })
                .verifyComplete();
    }
}
