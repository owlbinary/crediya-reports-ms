package com.crediya.usecase.getreportcounter;

import com.crediya.model.report.Report;
import com.crediya.model.report.gateways.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetReportCounterUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    private GetReportCounterUseCase getReportCounterUseCase;

    @BeforeEach
    void setUp() {
        getReportCounterUseCase = new GetReportCounterUseCase(reportRepository);
    }

    @Test
    void shouldGetApprovedLoansCountSuccessfully() {
        Report expectedReport = createReport(15L, new BigDecimal("75000000.00"));
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(expectedReport));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectNext(expectedReport)
                .verifyComplete();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldReturnReportWithZeroCountWhenNotFound() {
        Report emptyReport = createReport(0L, BigDecimal.ZERO);
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(emptyReport));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(0L) &&
                    report.getTotalApprovedAmount().equals(BigDecimal.ZERO) &&
                    report.getReportType().equals("APPROVED_LOANS")
                )
                .verifyComplete();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldMapRepositoryErrorToRuntimeException() {
        RuntimeException repositoryError = new RuntimeException("DynamoDB connection failed");
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.error(repositoryError));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Error al obtener el contador de préstamos aprobados") &&
                    error.getCause() == repositoryError
                )
                .verify();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldHandleDatabaseTimeoutError() {
        RuntimeException timeoutError = new RuntimeException("Database timeout");
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.error(timeoutError));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Error al obtener el contador de préstamos aprobados")
                )
                .verify();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldHandleReportWithLargeNumbers() {
        Report largeReport = createReport(9999999L, new BigDecimal("999999999999.99"));
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(largeReport));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(9999999L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("999999999999.99"))
                )
                .verifyComplete();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldUseCorrectReportType() {
        Report report = createReport(1L, new BigDecimal("1000000.00"));
        when(reportRepository.findByReportType(any()))
                .thenReturn(Mono.just(report));

        getReportCounterUseCase.obtenerContadorPrestamosAprobados().subscribe();

        verify(reportRepository).findByReportType("APPROVED_LOANS");
        verify(reportRepository, never()).findByReportType("OTHER_TYPE");
    }

    @Test
    void shouldPreserveReportMetadata() {
        LocalDateTime specificTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        Report reportWithMetadata = Report.builder()
                .id("APPROVED_LOANS")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(42L)
                .totalApprovedAmount(new BigDecimal("21000000.00"))
                .lastUpdated(specificTime)
                .build();

        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(reportWithMetadata));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getId().equals("APPROVED_LOANS") &&
                    report.getReportType().equals("APPROVED_LOANS") &&
                    report.getApprovedLoansCount().equals(42L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("21000000.00")) &&
                    report.getLastUpdated().equals(specificTime)
                )
                .verifyComplete();
    }

    @Test
    void shouldHandleNullAmountInReport() {
        Report reportWithNullAmount = Report.builder()
                .id("APPROVED_LOANS")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(5L)
                .totalApprovedAmount(null)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(reportWithNullAmount));

        Mono<Report> result = getReportCounterUseCase.obtenerContadorPrestamosAprobados();

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(5L) &&
                    report.getTotalApprovedAmount() == null
                )
                .verifyComplete();
    }

    @Test
    void shouldCallRepositoryOnlyOnce() {
        Report report = createReport(3L, new BigDecimal("3000000.00"));
        when(reportRepository.findByReportType(eq("APPROVED_LOANS")))
                .thenReturn(Mono.just(report));

        getReportCounterUseCase.obtenerContadorPrestamosAprobados().subscribe();

        verify(reportRepository, times(1)).findByReportType("APPROVED_LOANS");
    }

    private Report createReport(Long count, BigDecimal totalAmount) {
        return Report.builder()
                .id("APPROVED_LOANS")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(count)
                .totalApprovedAmount(totalAmount)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
