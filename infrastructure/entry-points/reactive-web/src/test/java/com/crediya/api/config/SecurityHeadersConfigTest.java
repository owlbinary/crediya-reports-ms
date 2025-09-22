package com.crediya.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityHeadersConfig - Configuración de Headers de Seguridad")
class SecurityHeadersConfigTest {

    @Mock
    private WebFilterChain chain;

    private SecurityHeadersConfig securityHeadersConfig;

    @BeforeEach
    void setUp() {
        securityHeadersConfig = new SecurityHeadersConfig();
    }

    @Test
    @DisplayName("Debe aplicar headers de seguridad correctamente")
    void debeAplicarHeadersDeSeguridadCorrectamente() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        HttpHeaders responseHeaders = mockExchange.getResponse().getHeaders();

        assertThat(responseHeaders.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; frame-ancestors 'self'; form-action 'self'");
        assertThat(responseHeaders.getFirst("Strict-Transport-Security"))
                .isEqualTo("max-age=31536000;");
        assertThat(responseHeaders.getFirst("X-Content-Type-Options"))
                .isEqualTo("nosniff");
        assertThat(responseHeaders.getFirst("Server"))
                .isEmpty();
        assertThat(responseHeaders.getFirst("Cache-Control"))
                .isEqualTo("no-store");
        assertThat(responseHeaders.getFirst("Pragma"))
                .isEqualTo("no-cache");
        assertThat(responseHeaders.getFirst("Referrer-Policy"))
                .isEqualTo("strict-origin-when-cross-origin");
        
        verify(chain).filter(mockExchange);
    }

    @Test
    @DisplayName("Debe continuar la cadena de filtros")
    void debeContinuarLaCadenaFiltros() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        verify(chain).filter(mockExchange);
    }

    @Test
    @DisplayName("Debe aplicar headers con MockServerWebExchange")
    void debeAplicarHeadersConMockServerWebExchange() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        HttpHeaders responseHeaders = mockExchange.getResponse().getHeaders();
        
        assertThat(responseHeaders.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; frame-ancestors 'self'; form-action 'self'");
        assertThat(responseHeaders.getFirst("Strict-Transport-Security"))
                .isEqualTo("max-age=31536000;");
        assertThat(responseHeaders.getFirst("X-Content-Type-Options"))
                .isEqualTo("nosniff");
        assertThat(responseHeaders.getFirst("Server"))
                .isEmpty();
        assertThat(responseHeaders.getFirst("Cache-Control"))
                .isEqualTo("no-store");
        assertThat(responseHeaders.getFirst("Pragma"))
                .isEqualTo("no-cache");
        assertThat(responseHeaders.getFirst("Referrer-Policy"))
                .isEqualTo("strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("Debe verificar que es un WebFilter válido")
    void debeVerificarQueEsUnWebFilterValido() {
        assertThat(securityHeadersConfig).isInstanceOf(org.springframework.web.server.WebFilter.class);
        assertNotNull(securityHeadersConfig);
    }

    @Test
    @DisplayName("Debe aplicar headers para diferentes tipos de request")
    void debeAplicarHeadersParaDiferentesTiposDeRequest() {
        MockServerHttpRequest getRequest = MockServerHttpRequest.get("/api/reportes").build();
        MockServerHttpRequest postRequest = MockServerHttpRequest.post("/api/reportes").build();
        MockServerHttpRequest putRequest = MockServerHttpRequest.put("/api/reportes/1").build();
        
        ServerWebExchange getExchange = MockServerWebExchange.from(getRequest);
        ServerWebExchange postExchange = MockServerWebExchange.from(postRequest);
        ServerWebExchange putExchange = MockServerWebExchange.from(putRequest);
        
        when(chain.filter(getExchange)).thenReturn(Mono.empty());
        when(chain.filter(postExchange)).thenReturn(Mono.empty());
        when(chain.filter(putExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(getExchange, chain))
                .verifyComplete();

        StepVerifier.create(securityHeadersConfig.filter(postExchange, chain))
                .verifyComplete();

        StepVerifier.create(securityHeadersConfig.filter(putExchange, chain))
                .verifyComplete();

        assertHeadersAreSet(getExchange.getResponse().getHeaders());
        assertHeadersAreSet(postExchange.getResponse().getHeaders());
        assertHeadersAreSet(putExchange.getResponse().getHeaders());
    }

    @Test
    @DisplayName("Debe manejar headers existentes correctamente")
    void debeManejarHeadersExistentesCorrectamente() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        mockExchange.getResponse().getHeaders().set("Custom-Header", "custom-value");
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        HttpHeaders responseHeaders = mockExchange.getResponse().getHeaders();
        
        assertHeadersAreSet(responseHeaders);
        
        assertThat(responseHeaders.getFirst("Custom-Header")).isEqualTo("custom-value");
    }

    @Test
    @DisplayName("Debe sobrescribir header Server existente")
    void debeSobrescribirHeaderServerExistente() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);

        mockExchange.getResponse().getHeaders().set("Server", "Apache/2.4.41");
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        HttpHeaders responseHeaders = mockExchange.getResponse().getHeaders();

        assertThat(responseHeaders.getFirst("Server")).isEmpty();
    }

    @Test
    @DisplayName("Debe aplicar headers para rutas públicas")
    void debeAplicarHeadersParaRutasPublicas() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/public/automatico").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        assertHeadersAreSet(mockExchange.getResponse().getHeaders());
    }

    @Test
    @DisplayName("Debe aplicar headers para rutas protegidas")
    void debeAplicarHeadersParaRutasProtegidas() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/reportes").build();
        ServerWebExchange mockExchange = MockServerWebExchange.from(request);
        
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(securityHeadersConfig.filter(mockExchange, chain))
                .verifyComplete();

        assertHeadersAreSet(mockExchange.getResponse().getHeaders());
    }

    private void assertHeadersAreSet(HttpHeaders headers) {
        assertThat(headers.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; frame-ancestors 'self'; form-action 'self'");
        assertThat(headers.getFirst("Strict-Transport-Security"))
                .isEqualTo("max-age=31536000;");
        assertThat(headers.getFirst("X-Content-Type-Options"))
                .isEqualTo("nosniff");
        assertThat(headers.getFirst("Server"))
                .isEmpty();
        assertThat(headers.getFirst("Cache-Control"))
                .isEqualTo("no-store");
        assertThat(headers.getFirst("Pragma"))
                .isEqualTo("no-cache");
        assertThat(headers.getFirst("Referrer-Policy"))
                .isEqualTo("strict-origin-when-cross-origin");
    }
}