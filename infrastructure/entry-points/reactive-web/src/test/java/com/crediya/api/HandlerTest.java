package com.crediya.api;

import com.crediya.model.report.Report;
import com.crediya.usecase.getreportcounter.GetReportCounterUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private GetReportCounterUseCase getReportCounterUseCase;

    private Handler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new Handler(getReportCounterUseCase);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void shouldReturnApprovedLoansReportSuccessfully() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        Report report = createReport(25L, new BigDecimal("125000000.50"), now);
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(report));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(200, response.statusCode().value());
                    return true;
                })
                .verifyComplete();

        verify(getReportCounterUseCase).obtenerContadorPrestamosAprobados();
    }

    @Test
    void shouldReturnCorrectReportResponseStructure() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        Report report = createReport(42L, new BigDecimal("210000000.75"), now);
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(report));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(200, response.statusCode().value());
                    MediaType contentType = response.headers().getContentType();
                    assertTrue(contentType != null && contentType.toString().contains("application/json"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleUseCaseErrorAndReturnInternalServerError() {
        RuntimeException useCaseError = new RuntimeException("Database connection failed");
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.error(useCaseError));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(500, response.statusCode().value());
                    MediaType contentType = response.headers().getContentType();
                    assertTrue(contentType != null && contentType.toString().contains("application/json"));
                    return true;
                })
                .verifyComplete();

        verify(getReportCounterUseCase).obtenerContadorPrestamosAprobados();
    }

    @Test
    void shouldHandleNullPointerExceptionFromUseCase() {
        NullPointerException nullPointerError = new NullPointerException("Null report data");
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.error(nullPointerError));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.statusCode().value() == 500
                )
                .verifyComplete();
    }

    @Test
    void shouldHandleReportWithZeroValues() {
        Report reportWithZeros = createReport(0L, BigDecimal.ZERO, LocalDateTime.now());
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(reportWithZeros));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.statusCode().value() == 200
                )
                .verifyComplete();
    }

    @Test
    void shouldHandleReportWithNullAmount() {
        Report reportWithNullAmount = Report.builder()
                .id("APPROVED_LOANS")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(15L)
                .totalApprovedAmount(null)
                .lastUpdated(LocalDateTime.now())
                .build();
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(reportWithNullAmount));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.statusCode().value() == 200
                )
                .verifyComplete();
    }

    @Test
    void shouldHandleReportWithLargeNumbers() {
        Report largeReport = createReport(999999999L, new BigDecimal("999999999999999.99"), LocalDateTime.now());
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(largeReport));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.statusCode().value() == 200
                )
                .verifyComplete();
    }

    @Test
    void shouldSetCorrectContentTypeForSuccessResponse() {
        Report report = createReport(10L, new BigDecimal("50000000.00"), LocalDateTime.now());
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(report));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    MediaType contentType = response.headers().getContentType();
                    return contentType != null && contentType.equals(MediaType.APPLICATION_JSON);
                })
                .verifyComplete();
    }

    @Test
    void shouldSetCorrectContentTypeForErrorResponse() {
        RuntimeException error = new RuntimeException("Service unavailable");
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.error(error));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    MediaType contentType = response.headers().getContentType();
                    return contentType != null && contentType.equals(MediaType.APPLICATION_JSON) &&
                           response.statusCode().value() == 500;
                })
                .verifyComplete();
    }

    @Test
    void shouldCallUseCaseOnlyOnce() {
        Report report = createReport(5L, new BigDecimal("25000000.00"), LocalDateTime.now());
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.just(report));

        handler.obtenerReportePrestamosAprobados(request).subscribe();

        verify(getReportCounterUseCase, times(1)).obtenerContadorPrestamosAprobados();
    }

    @Test
    void shouldHandleTimeout() {
        ServerRequest request = MockServerRequest.builder().build();

        when(getReportCounterUseCase.obtenerContadorPrestamosAprobados())
                .thenReturn(Mono.error(new RuntimeException("Request timeout")));

        Mono<ServerResponse> result = handler.obtenerReportePrestamosAprobados(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.statusCode().value() == 500
                )
                .verifyComplete();
    }

    private Report createReport(Long count, BigDecimal totalAmount, LocalDateTime lastUpdated) {
        return Report.builder()
                .id("APPROVED_LOANS")
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(count)
                .totalApprovedAmount(totalAmount)
                .lastUpdated(lastUpdated)
                .build();
    }
}
