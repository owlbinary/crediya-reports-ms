package com.crediya.usecase.eventos;

import com.crediya.model.event.LoanApprovedEvent;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarSolicitudAprobadaUseCaseTest {

    @Mock
    private IncrementReportCounterUseCase incrementreportcounterUseCase;

    private ProcesarSolicitudAprobadaUseCase procesarSolicitudAprobadaUseCase;

    @BeforeEach
    void setUp() {
        procesarSolicitudAprobadaUseCase = new ProcesarSolicitudAprobadaUseCase(incrementreportcounterUseCase);
    }

    @Test
    void shouldProcessApprovedLoanEventSuccessfully() {
        LoanApprovedEvent event = createValidLoanApprovedEvent();
        when(incrementreportcounterUseCase.incrementarContador(any(LoanApprovedEvent.class)))
                .thenReturn(Mono.empty());

        Mono<Void> result = procesarSolicitudAprobadaUseCase.procesar(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase).incrementarContador(event);
    }

    @Test
    void shouldPropagateErrorFromIncrementUseCase() {
        LoanApprovedEvent event = createValidLoanApprovedEvent();
        RuntimeException expectedException = new RuntimeException("Database error");
        when(incrementreportcounterUseCase.incrementarContador(any(LoanApprovedEvent.class)))
                .thenReturn(Mono.error(expectedException));

        Mono<Void> result = procesarSolicitudAprobadaUseCase.procesar(event);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(incrementreportcounterUseCase).incrementarContador(event);
    }

    @Test
    void shouldHandleNullEventGracefully() {
        when(incrementreportcounterUseCase.incrementarContador(any()))
                .thenReturn(Mono.empty());

        Mono<Void> result = procesarSolicitudAprobadaUseCase.procesar(null);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase).incrementarContador(null);
    }

    @Test
    void shouldProcessEventWithNullAmount() {
        LoanApprovedEvent event = createEventWithNullAmount();
        when(incrementreportcounterUseCase.incrementarContador(any(LoanApprovedEvent.class)))
                .thenReturn(Mono.empty());

        Mono<Void> result = procesarSolicitudAprobadaUseCase.procesar(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase).incrementarContador(event);
    }

    @Test
    void shouldProcessEventWithZeroAmount() {
        LoanApprovedEvent event = createEventWithZeroAmount();
        when(incrementreportcounterUseCase.incrementarContador(any(LoanApprovedEvent.class)))
                .thenReturn(Mono.empty());

        Mono<Void> result = procesarSolicitudAprobadaUseCase.procesar(event);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase).incrementarContador(event);
    }

    private LoanApprovedEvent createValidLoanApprovedEvent() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(new BigDecimal("5000000.00"))
                .plazo(36)
                .build();

        return LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
    }

    private LoanApprovedEvent createEventWithNullAmount() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(null)
                .plazo(36)
                .build();

        return LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
    }

    private LoanApprovedEvent createEventWithZeroAmount() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(BigDecimal.ZERO)
                .plazo(36)
                .build();

        return LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
    }
}
