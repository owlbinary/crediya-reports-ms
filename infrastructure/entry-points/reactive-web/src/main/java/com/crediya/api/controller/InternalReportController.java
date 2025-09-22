package com.crediya.api.controller;

import com.crediya.api.service.AutomaticReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para endpoints internos de reportes sin autenticación.
 */
@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Validated
public class InternalReportController {
    
    private final AutomaticReportService automaticReportService;

    @PostMapping(value = "/automatico", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> generarReporteAutomatico() {
        log.info("Procesando solicitud de generación automática de reporte de rendimiento");
        
        return automaticReportService.generarYEnviarReporte()
                .map(messageId -> "Reporte generado y enviado exitosamente con ID: " + messageId)
                .doOnSuccess(response -> log.info("Reporte automático procesado exitosamente con respuesta: {}", response))
                .doOnError(error -> log.error("Error generando reporte automático: {}", error.getMessage()));
    }
}