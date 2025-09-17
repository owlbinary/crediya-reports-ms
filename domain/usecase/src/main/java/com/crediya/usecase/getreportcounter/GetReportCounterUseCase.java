package com.crediya.usecase.getreportcounter;

import com.crediya.model.report.Report;
import com.crediya.model.report.gateways.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@RequiredArgsConstructor
public class GetReportCounterUseCase {
    
    private static final String APPROVED_LOANS_REPORT_TYPE = "APPROVED_LOANS";
    private static final String ERROR_MESSAGE = "Error al obtener el contador de préstamos aprobados";
    
    private final ReportRepository reportRepository;

    public Mono<Report> obtenerContadorPrestamosAprobados() {
        log.info("Obteniendo contador de préstamos aprobados");
        
        return reportRepository.findByReportType(APPROVED_LOANS_REPORT_TYPE)
                .doOnSuccess(report -> log.info("Reporte obtenido con contador: " + report.getApprovedLoansCount()))
                .doOnError(error -> log.severe("Error obteniendo reporte: " + error.getMessage()))
                .onErrorMap(error -> new RuntimeException(ERROR_MESSAGE, error));
    }
}
