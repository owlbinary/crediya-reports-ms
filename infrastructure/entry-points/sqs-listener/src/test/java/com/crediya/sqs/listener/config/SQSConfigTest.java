package com.crediya.sqs.listener.config;

import com.crediya.model.event.LoanApprovedEvent;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSConfigTest {

    @Mock
    private IncrementReportCounterUseCase incrementreportcounterUseCase;

    private SQSConfig sqsConfig;

    @BeforeEach
    void setUp() {
        sqsConfig = new SQSConfig();
    }

    @Test
    void shouldProcessApprovedLoanMessage() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "justificacion": "Procesado por validación automática de capacidad de endeudamiento",
                "origen": "debt-capacity-lambda",
                "monto": "100000.00",
                "tasaInteres": "0.18",
                "plazo": 48,
                "planPago": [
                  {
                    "numero_cuota": 1,
                    "cuota": "2937.50",
                    "abono_capital": "1437.50",
                    "interes": "1500.00",
                    "saldo_restante": "98562.50"
                  },
                  {
                    "numero_cuota": 2,
                    "cuota": "2937.50",
                    "abono_capital": "1459.06",
                    "interes": "1478.44",
                    "saldo_restante": "97103.44"
                  }
                ]
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("61", capturedEvent.getLoanId());
        assertEquals(100000.0, capturedEvent.getAmount());
        assertEquals("APROBADO", capturedEvent.getStatus());
    }

    @Test
    void shouldIgnoreNonActualizarEstadoSolicitudMessages() {
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "otro_tipo",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase, never()).incrementarContador(any());
    }

    @Test
    void shouldIgnoreNonAprobadoMessages() {
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "RECHAZADO",
                "justificacion": "Procesado por validación automática de capacidad de endeudamiento",
                "origen": "debt-capacity-lambda"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase, never()).incrementarContador(any());
    }

    @Test
    void shouldHandleInvalidAmountGracefully() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "justificacion": "Procesado por validación automática de capacidad de endeudamiento",
                "origen": "debt-capacity-lambda"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("61", capturedEvent.getLoanId());
        assertNull(capturedEvent.getAmount());
    }

    @Test
    void shouldHandleMalformedJsonGracefully() {
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = "{ invalid json }";
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase, never()).incrementarContador(any());
    }

    @Test
    void shouldCalculateAmountFromPlanPagoWhenMontoIsNull() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "planPago": [
                  {
                    "numero_cuota": 1,
                    "cuota": "2500.00",
                    "abono_capital": "2000.00",
                    "interes": "500.00",
                    "saldo_restante": "98000.00"
                  }
                ]
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(100000.0, capturedEvent.getAmount());
    }

    @Test
    void shouldHandleEmptyPlanPago() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "planPago": []
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getAmount());
        assertNull(capturedEvent.getParams().getPlanPago());
    }

    @Test
    void shouldHandleNullParamsGracefully() {
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": null
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase, never()).incrementarContador(any());
    }

    @Test
    void shouldHandleInvalidAmountInPlanPago() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "planPago": [
                  {
                    "numero_cuota": 1,
                    "cuota": "invalid_number",
                    "abono_capital": null,
                    "interes": "500.00",
                    "saldo_restante": "invalid_amount"
                  }
                ]
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getAmount());
    }

    @Test
    void shouldHandleNumericAmountAsString() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": "50000.50"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(50000.5, capturedEvent.getAmount());
    }

    @Test
    void shouldHandleNumericAmountAsNumber() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": 75000.75
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(75000.75, capturedEvent.getAmount());
    }

    @Test
    void shouldHandleInvalidAmountString() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": "invalid_amount"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getAmount());
    }

    @Test
    void shouldHandleUseCaseErrorGracefully() {
        when(incrementreportcounterUseCase.incrementarContador(any()))
                .thenReturn(Mono.error(new RuntimeException("UseCase error")));
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": "100000.00"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);

        verify(incrementreportcounterUseCase).incrementarContador(any());
    }

    @Test
    void shouldHandleGenericExceptionInProcessing() {
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        Message nullMessage = null;

        Mono<Void> result = messageProcessor.apply(nullMessage);

        StepVerifier.create(result)
                .verifyComplete();

        verify(incrementreportcounterUseCase, never()).incrementarContador(any());
    }

    @Test
    void shouldHandleIntegerPlazoAsString() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": "100000.00",
                "plazo": "48"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(48, capturedEvent.getParams().getPlazo());
    }

    @Test
    void shouldHandleInvalidPlazoValue() {
        when(incrementreportcounterUseCase.incrementarContador(any())).thenReturn(Mono.empty());
        Function<Message, Mono<Void>> messageProcessor = sqsConfig.messageProcessor(incrementreportcounterUseCase);
        
        String messageBody = """
            {
              "tipo": "actualizar_estado_solicitud",
              "params": {
                "solicitudId": "61",
                "nuevoEstado": "APROBADO",
                "monto": "100000.00",
                "plazo": "invalid_plazo"
              }
            }
            """;
        
        Message message = Message.builder()
                .body(messageBody)
                .build();

        Mono<Void> result = messageProcessor.apply(message);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LoanApprovedEvent> eventCaptor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(incrementreportcounterUseCase).incrementarContador(eventCaptor.capture());
        
        LoanApprovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(0, capturedEvent.getParams().getPlazo());
    }
}
