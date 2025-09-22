package com.crediya.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private WebFilterChain chain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
    }

    @Test
    void deberiaCrearFiltroCorrectamente() {
        assertThat(jwtAuthenticationFilter).isNotNull();
    }

    @Test
    void deberiaFilterarSinTokenYManejarNoAutorizado() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaFilterarConTokenVacioYManejarNoAutorizado() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaFilterarConTokenSinBearerYManejarNoAutorizado() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "InvalidToken")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaFilterarConTokenValidoYContinuarCadena() {
        String token = "Bearer valid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.extraerEmail("valid-jwt-token")).thenReturn("admin@test.com");
        when(jwtService.extraerIdUsuario("valid-jwt-token")).thenReturn("123");
        when(jwtService.extraerNombre("valid-jwt-token")).thenReturn("Juan");
        when(jwtService.extraerApellido("valid-jwt-token")).thenReturn("Pérez");
        when(jwtService.extraerIdRol("valid-jwt-token")).thenReturn("1");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("valid-jwt-token");
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void deberiaPermitirRutasPublicasSinAutenticacion() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirSwaggerUI() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirApiDocs() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/v3/api-docs/swagger-config").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaManejartokenInvalidoYRetornarNoAutorizado() {
        String token = "Bearer invalid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("invalid-jwt-token")).thenReturn(false);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("invalid-jwt-token");
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejartokenConErrorYRetornarNoAutorizado() {
        String token = "Bearer error-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("error-jwt-token")).thenReturn(false);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("error-jwt-token");
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaTenerConstructorPublico() throws Exception {
        Constructor<JwtAuthenticationFilter> constructor = 
                JwtAuthenticationFilter.class.getDeclaredConstructor(JwtService.class);
        assertThat(Modifier.isPublic(constructor.getModifiers())).isTrue();
    }

    @Test
    void deberiaTenerNombreCorrectoDeLaClase() {
        assertThat(JwtAuthenticationFilter.class.getSimpleName()).isEqualTo("JwtAuthenticationFilter");
    }

    @Test
    void deberiaImplementarWebFilter() {
        assertThat(org.springframework.web.server.WebFilter.class)
                .isAssignableFrom(JwtAuthenticationFilter.class);
    }

    @Test
    void deberiaBloquearEndpointReportesParaUsuarioSinToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/reportes").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaPermitirRutasPublicasConPrefijoReports() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/reports/actuator/health").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirSwaggerUIConPrefijoReports() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/reports/swagger-ui/index.html").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirApiDocsConPrefijoReports() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/reports/v3/api-docs/swagger-config").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirWebjarsConPrefijoReports() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/reports/webjars/swagger-ui/index.css").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirWebjars() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/webjars/swagger-ui/index.css").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirRutasPublicConPrefijoReports() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/reports/public/automatico").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaPermitirRutasPublic() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/public/automatico").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void deberiaManejarHeaderAuthorizationNull() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarTokenBearerVacio() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "Bearer ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarTokenBearerSoloConEspacios() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "Bearer    ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarHeaderSinTexto() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "   ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarExcepcionEnExtraerDatosDelToken() {
        String token = "Bearer valid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.extraerEmail("valid-jwt-token")).thenThrow(new RuntimeException("Error extrayendo email"));

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("valid-jwt-token");
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarExcepcionEnExtraerIdUsuario() {
        String token = "Bearer valid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.extraerEmail("valid-jwt-token")).thenReturn("test@test.com");
        when(jwtService.extraerIdUsuario("valid-jwt-token")).thenThrow(new RuntimeException("Error extrayendo ID"));

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("valid-jwt-token");
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarExcepcionEnExtraerNombre() {
        String token = "Bearer valid-jwt-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtService.validarToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.extraerEmail("valid-jwt-token")).thenReturn("test@test.com");
        when(jwtService.extraerIdUsuario("valid-jwt-token")).thenReturn("123");
        when(jwtService.extraerNombre("valid-jwt-token")).thenThrow(new RuntimeException("Error extrayendo nombre"));

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtService, times(1)).validarToken("valid-jwt-token");
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarTokenConFormatoBearerIncorrecto() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "bear token-here")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deberiaManejarTokenConBearerEnMinuscula() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("Authorization", "bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
