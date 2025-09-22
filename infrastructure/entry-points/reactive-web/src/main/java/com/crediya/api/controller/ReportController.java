package com.crediya.api.controller;

import com.crediya.api.dto.ReportResponseDto;
import com.crediya.api.security.JwtUserPrincipal;
import com.crediya.api.service.AutomaticReportService;
import com.crediya.model.report.Report;
import com.crediya.usecase.getreportcounter.GetReportCounterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para gestión de reportes.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reportes", description = "API para consulta de reportes del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final GetReportCounterUseCase getReportCounterUseCase;
    private final AutomaticReportService automaticReportService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener reporte de préstamos aprobados", 
               description = "Obtiene el reporte con la cantidad total de préstamos aprobados. Solo accesible para usuarios con rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte obtenido exitosamente", 
                    content = @Content(schema = @Schema(implementation = ReportResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Mono<ReportResponseDto> obtenerReportePrestamosAprobados(Authentication authentication) {
        JwtUserPrincipal userPrincipal = (JwtUserPrincipal) authentication.getPrincipal();
        log.info("Procesando solicitud de reporte de préstamos aprobados por usuario ADMIN: {}", userPrincipal.getEmail());
        
        return getReportCounterUseCase.obtenerContadorPrestamosAprobados()
                .map(this::toReportResponse)
                .doOnSuccess(response -> log.info("Reporte de préstamos aprobados devuelto exitosamente para usuario: {}", userPrincipal.getEmail()))
                .doOnError(error -> log.error("Error procesando solicitud de reporte para usuario {}: {}", userPrincipal.getEmail(), error.getMessage()));
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
}
