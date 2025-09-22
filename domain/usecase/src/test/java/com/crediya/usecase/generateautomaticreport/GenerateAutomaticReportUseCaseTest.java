package com.crediya.usecase.generateautomaticreport;

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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateAutomaticReportUseCaseTest {

    private static final String ADMIN_EMAILS = "admin1@test.com,admin2@test.com";

    @Mock
    private ReportRepository reportRepository;

    private GenerateAutomaticReportUseCase generateAutomaticReportUseCase;

    @BeforeEach
    void setUp() {
        generateAutomaticReportUseCase = new GenerateAutomaticReportUseCase(reportRepository, ADMIN_EMAILS);
    }

    @Test
    void shouldGenerateAutomaticReportSuccessfully() {
        Report report = createReport(150L, new BigDecimal("75000000.50"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"email\":") &&
                           mensaje.contains("admin1@test.com") &&
                           mensaje.contains("admin2@test.com") &&
                           mensaje.contains("\"asunto\":") &&
                           mensaje.contains("\"contenido\":") &&
                           mensaje.contains("\"fechaGeneracion\":") &&
                           mensaje.contains("150") &&
                           mensaje.contains("75,000,000.50");
                })
                .verifyComplete();
        verify(reportRepository).findByReportType("APPROVED_LOANS");
    }

    @Test
    void shouldHandleZeroValues() {
        Report report = createReport(0L, BigDecimal.ZERO);
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"asunto\":") &&
                           mensaje.contains("\"contenido\":") &&
                           mensaje.contains("0");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleLargeNumbers() {
        Report report = createReport(9999999L, new BigDecimal("999999999999.99"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("9,999,999") &&
                           mensaje.contains("999,999,999,999.99") &&
                           mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"asunto\":");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleNullAmountInReport() {
        Report report = Report.builder()
                .id("1")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(5L)
                .totalApprovedAmount(null)
                .lastUpdated(LocalDateTime.now())
                .build();
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"contenido\":") &&
                           mensaje.contains("5");
                })
                .verifyComplete();
    }

    @Test
    void shouldEscapeSpecialCharactersInContent() {
        Report report = createReport(5L, new BigDecimal("100000.00"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"contenido\":") &&
                           mensaje.contains("\"tipo\":") &&
                           mensaje.contains("Total de préstamos aprobados: 5");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleNullCountInReport() {
        Report report = Report.builder()
                .id("2")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(null)
                .totalApprovedAmount(new BigDecimal("50000.00"))
                .lastUpdated(LocalDateTime.now())
                .build();
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"contenido\":") &&
                           mensaje.contains("50,000.00");
                })
                .verifyComplete();
    }

    @Test
    void shouldIncludeCorrectSubjectInMessage() {
        Report report = createReport(42L, new BigDecimal("1500000.75"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"asunto\":") &&
                           mensaje.contains("Reporte");
                })
                .verifyComplete();
    }

    @Test
    void shouldIncludeDateFormattingInMessage() {
        Report report = createReport(3L, new BigDecimal("75000.00"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"fechaGeneracion\":") &&
                           mensaje.contains("\"") &&
                           !mensaje.contains("null");
                })
                .verifyComplete();
    }

    @Test
    void shouldIncludeAllAdminEmails() {
        Report report = createReport(25L, new BigDecimal("2500000.00"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("admin1@test.com") &&
                           mensaje.contains("admin2@test.com") &&
                           mensaje.contains("\"email\":");
                })
                .verifyComplete();
    }

    @Test
    void shouldContainValidJsonStructure() {
        Report report = createReport(10L, new BigDecimal("100000.00"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.trim().startsWith("{") &&
                           mensaje.trim().endsWith("}") &&
                           mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"email\":") &&
                           mensaje.contains("\"asunto\":") &&
                           mensaje.contains("\"contenido\":") &&
                           mensaje.contains("\"fechaGeneracion\":");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyAdminEmails() {
        GenerateAutomaticReportUseCase useCaseWithEmptyEmails = 
                new GenerateAutomaticReportUseCase(reportRepository, "");
        Report report = createReport(10L, new BigDecimal("50000.00"));
        when(reportRepository.findByReportType("APPROVED_LOANS"))
                .thenReturn(Mono.just(report));
        Mono<String> result = useCaseWithEmptyEmails.generarReporteAutomatico();
        StepVerifier.create(result)
                .expectNextMatches(mensaje -> {
                    return mensaje.contains("\"email\": \"\"") &&
                           mensaje.contains("\"tipo\":") &&
                           mensaje.contains("\"contenido\":");
                })
                .verifyComplete();
    }

        @Test
        void shouldValidateReportTypeUsedInRepository() {
                Report report = createReport(100L, new BigDecimal("5000000.00"));
                when(reportRepository.findByReportType("APPROVED_LOANS"))
                                .thenReturn(Mono.just(report));
                generateAutomaticReportUseCase.generarReporteAutomatico().block();
                verify(reportRepository, times(1)).findByReportType("APPROVED_LOANS");
                verifyNoMoreInteractions(reportRepository);
        }

        @Test
        void shouldContainPerformanceReportType() {
                Report report = createReport(50L, new BigDecimal("2500000.00"));
                when(reportRepository.findByReportType("APPROVED_LOANS"))
                                .thenReturn(Mono.just(report));
                Mono<String> result = generateAutomaticReportUseCase.generarReporteAutomatico();
                StepVerifier.create(result)
                                .expectNextMatches(mensaje -> {
                                        return mensaje.contains("\"tipo\": \"reporte_rendimiento\"");
                                })
                                .verifyComplete();
        }

    private Report createReport(Long count, BigDecimal amount) {
        return Report.builder()
                .id("1")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(count)
                .totalApprovedAmount(amount)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}