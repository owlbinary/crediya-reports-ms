package com.crediya.api;

import com.crediya.api.dto.ErrorResponseDto;
import com.crediya.api.dto.ReportResponseDto;
import com.crediya.model.report.Report;
import com.crediya.usecase.getreportcounter.GetReportCounterUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Log
@Component
@RequiredArgsConstructor
public class Handler {
    
    private final GetReportCounterUseCase getReportCounterUseCase;

    public Mono<ServerResponse> obtenerReportePrestamosAprobados(ServerRequest request) {
        log.info("Procesando solicitud para obtener reporte de préstamos aprobados");
        
        return getReportCounterUseCase.obtenerContadorPrestamosAprobados()
                .map(this::toReportResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.info("Reporte de préstamos aprobados devuelto exitosamente"))
                .onErrorResume(error -> {
                    log.severe("Error procesando solicitud de reporte de préstamos aprobados: " + error.getMessage());
                    return manejarError(error);
                });
    }

    private ReportResponseDto toReportResponse(Report report) {
        return ReportResponseDto.builder()
                .reportType(report.getReportType())
                .approvedLoansCount(report.getApprovedLoansCount())
                .totalApprovedAmount(report.getTotalApprovedAmount())
                .lastUpdated(report.getLastUpdated())
                .message("Cantidad total de préstamos aprobados obtenida exitosamente")
                .build();
    }

    private Mono<ServerResponse> manejarError(Throwable error) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("Error interno del servidor. Intente nuevamente más tarde.")
                .status(500)
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ServerResponse.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }
}
