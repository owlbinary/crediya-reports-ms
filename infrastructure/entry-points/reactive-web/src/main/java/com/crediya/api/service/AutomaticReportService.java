package com.crediya.api.service;

import com.crediya.usecase.generateautomaticreport.GenerateAutomaticReportUseCase;
import com.crediya.sqs.sender.SQSSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class AutomaticReportService {
    
    private final GenerateAutomaticReportUseCase generateAutomaticReportUseCase;
    private final SQSSender sqsSender;
    
    public Mono<String> generarYEnviarReporte() {
        log.info("Iniciando proceso de generación y envío de reporte automático");
        
        return generateAutomaticReportUseCase.generarReporteAutomatico()
                .flatMap(mensajeReporte -> {
                    log.info("Reporte generado, enviando a cola de notificaciones");
                    return sqsSender.send(mensajeReporte);
                })
                .doOnSuccess(messageId -> log.info("Reporte enviado exitosamente a SQS con ID: {}", messageId))
                .doOnError(error -> log.error("Error en el proceso de reporte automático: {}", error.getMessage()));
    }
}