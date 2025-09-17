package com.crediya.usecase.incrementreportcounter;

import com.crediya.model.event.LoanApprovedEvent;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncrementReportCounterUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    private IncrementReportCounterUseCase incrementreportcounterUseCase;

    @BeforeEach
    void setUp() {
        incrementreportcounterUseCase = new IncrementReportCounterUseCase(reportRepository);
    }

    @Test
    void shouldIncrementCounterWithValidAmount() {
        LoanApprovedEvent event = createEventWithAmount(new BigDecimal("5000000.00"));
        Report updatedReport = createReport(10L, new BigDecimal("15000000.00"));
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(5000000.0));
    }

    @Test
    void shouldIncrementCounterWithNullAmount() {
        LoanApprovedEvent event = createEventWithAmount(null);
        Report updatedReport = createReport(5L, new BigDecimal("10000000.00"));
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.ZERO);
    }

    @Test
    void shouldIncrementCounterWithZeroAmount() {
        LoanApprovedEvent event = createEventWithAmount(BigDecimal.ZERO);
        Report updatedReport = createReport(3L, new BigDecimal("5000000.00"));
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(0.0));
    }

    @Test
    void shouldHandleRepositoryErrorGracefully() {
        LoanApprovedEvent event = createEventWithAmount(new BigDecimal("1000000.00"));
        RuntimeException repositoryError = new RuntimeException("DynamoDB connection error");
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.error(repositoryError));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(1000000.0));
    }

    @Test
    void shouldReturnEmptyOnRepositoryError() {
        LoanApprovedEvent event = createEventWithAmount(new BigDecimal("2000000.00"));
        RuntimeException repositoryError = new RuntimeException("Database timeout");
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.error(repositoryError));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(2000000.0));
    }

    @Test
    void shouldHandleEventWithNullParams() {
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(null)
                .build();
        
        Report updatedReport = createReport(1L, BigDecimal.ZERO);
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.ZERO);
    }



    @Test
    void shouldConvertDoubleAmountToBigDecimal() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("123")
                .estado("APROBADO")
                .monto(new BigDecimal("7500000.50"))
                .build();
        
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
        
        Report updatedReport = createReport(2L, new BigDecimal("12500000.50"));
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(7500000.5));
    }

    @Test
    void shouldIncrementCounterForLargeAmount() {
        BigDecimal largeAmount = new BigDecimal("999999999.99");
        LoanApprovedEvent event = createEventWithAmount(largeAmount);
        Report updatedReport = createReport(100L, new BigDecimal("50000000000.00"));
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(9.9999999999E8));
    }

    @Test
    void shouldIncrementCounterForNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-1000.00");
        LoanApprovedEvent event = createEventWithAmount(negativeAmount);
        Report updatedReport = createReport(1L, new BigDecimal("4999000.00"));
        
        when(reportRepository.incrementCounterAndAmount(eq("APPROVED_LOANS"), any(BigDecimal.class)))
                .thenReturn(Mono.just(updatedReport));

        Mono<Void> result = incrementreportcounterUseCase.incrementarContador(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(reportRepository).incrementCounterAndAmount("APPROVED_LOANS", BigDecimal.valueOf(-1000.0));
    }

    private LoanApprovedEvent createEventWithAmount(BigDecimal amount) {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(amount)
                .plazo(36)
                .build();

        return LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
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
