package com.crediya.usecase.generateautomaticreport;

import com.crediya.model.report.Report;
import com.crediya.model.report.gateways.ReportRepository;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log
public class GenerateAutomaticReportUseCase {
    
    private static final String APPROVED_LOANS_REPORT_TYPE = "APPROVED_LOANS";
    private static final String PERFORMANCE_REPORT_TYPE = "reporte_rendimiento";
    private static final String ERROR_MESSAGE = "Error al generar el reporte automático de rendimiento";
    
    private final ReportRepository reportRepository;
    private final String adminEmails;
    
    public GenerateAutomaticReportUseCase(ReportRepository reportRepository, String adminEmails) {
        this.reportRepository = reportRepository;
        this.adminEmails = adminEmails;
    }

    public Mono<String> generarReporteAutomatico() {
        log.info("Iniciando generación de reporte automático de rendimiento");
        
        return reportRepository.findByReportType(APPROVED_LOANS_REPORT_TYPE)
                .map(this::crearMensajeNotificacion)
                .doOnSuccess(mensaje -> log.info("Mensaje de reporte automático generado exitosamente"))
                .doOnError(error -> log.severe("Error generando reporte automático: " + error.getMessage()))
                .onErrorMap(error -> new RuntimeException(ERROR_MESSAGE, error));
    }
    
    private String crearMensajeNotificacion(Report report) {
        String reporteFormateado = formatearReporte(report);
        
        return String.format("""
            {
                "tipo": "%s",
                "email": "%s",
                "asunto": "Reporte Diario de Rendimiento - %s",
                "contenido": "%s",
                "fechaGeneracion": "%s"
            }
            """, 
            PERFORMANCE_REPORT_TYPE, 
            adminEmails,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            reporteFormateado.replace("\"", "\\\"").replace("\n", "\\n"),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    private String formatearReporte(Report report) {
        return String.format("""
            REPORTE DIARIO DE RENDIMIENTO DEL NEGOCIO
            
            MÉTRICAS PRINCIPALES:
            • Total de préstamos aprobados: %,d
            • Monto total prestado: $%,.2f
            • Fecha de última actualización: %s
            
            =============================================
            Generado el: %s
            """,
            report.getApprovedLoansCount(),
            report.getTotalApprovedAmount(),
            report.getLastUpdated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }
}