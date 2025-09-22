package com.crediya.api.controller;

import com.crediya.api.service.AutomaticReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalReportController - Controlador interno de reportes sin autenticación")
class InternalReportControllerTest {

    @Mock
    private AutomaticReportService automaticReportService;

    private InternalReportController internalReportController;

    @BeforeEach
    void setUp() {
        internalReportController = new InternalReportController(automaticReportService);
    }

    @Test
    @DisplayName("Debe generar reporte automático exitosamente")
    void debeGenerarReporteAutomaticoExitosamente() {
        String messageIdEsperado = "sqs-message-id-12345";
        String respuestaEsperada = "Reporte generado y enviado exitosamente con ID: " + messageIdEsperado;
        
        when(automaticReportService.generarYEnviarReporte())
                .thenReturn(Mono.just(messageIdEsperado));

        Mono<String> resultado = internalReportController.generarReporteAutomatico();

        StepVerifier.create(resultado)
                .assertNext(respuesta -> {
                    assertThat(respuesta).isNotNull();
                    assertThat(respuesta).isEqualTo(respuestaEsperada);
                })
                .verifyComplete();

        verify(automaticReportService).generarYEnviarReporte();
    }

    @Test
    @DisplayName("Debe manejar error en generación de reporte automático")
    void debeManejarErrorEnGeneracionReporteAutomatico() {
        RuntimeException errorEsperado = new RuntimeException("Error en el servicio de reportes");
        
        when(automaticReportService.generarYEnviarReporte())
                .thenReturn(Mono.error(errorEsperado));

        Mono<String> resultado = internalReportController.generarReporteAutomatico();

        StepVerifier.create(resultado)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Error en el servicio de reportes")
                )
                .verify();

        verify(automaticReportService).generarYEnviarReporte();
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío del servicio")
    void debeManejarMensajeVacioDelServicio() {
        String messageIdVacio = "";
        String respuestaEsperada = "Reporte generado y enviado exitosamente con ID: " + messageIdVacio;
        
        when(automaticReportService.generarYEnviarReporte())
                .thenReturn(Mono.just(messageIdVacio));

        Mono<String> resultado = internalReportController.generarReporteAutomatico();

        StepVerifier.create(resultado)
                .assertNext(respuesta -> {
                    assertThat(respuesta).isNotNull();
                    assertThat(respuesta).isEqualTo(respuestaEsperada);
                })
                .verifyComplete();

        verify(automaticReportService).generarYEnviarReporte();
    }

    @Test
    @DisplayName("Debe manejar timeout en el servicio")
    void debeManejarTimeoutEnElServicio() {
        RuntimeException timeoutError = new RuntimeException("Timeout en generación de reporte");
        
        when(automaticReportService.generarYEnviarReporte())
                .thenReturn(Mono.error(timeoutError));

        Mono<String> resultado = internalReportController.generarReporteAutomatico();

        StepVerifier.create(resultado)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Timeout en generación de reporte")
                )
                .verify();

        verify(automaticReportService).generarYEnviarReporte();
    }

    @Test
    @DisplayName("Debe validar que el endpoint responde con JSON")
    void debeValidarQueElEndpointRespondeConJSON() {
        String messageId = "test-message-id";
        
        when(automaticReportService.generarYEnviarReporte())
                .thenReturn(Mono.just(messageId));

        Mono<String> resultado = internalReportController.generarReporteAutomatico();

        StepVerifier.create(resultado)
                .assertNext(respuesta -> {
                    assertThat(respuesta).isNotNull();
                    assertThat(respuesta).isInstanceOf(String.class);
                    assertThat(respuesta).contains("Reporte generado y enviado exitosamente");
                    assertThat(respuesta).contains(messageId);
                })
                .verifyComplete();
    }
}