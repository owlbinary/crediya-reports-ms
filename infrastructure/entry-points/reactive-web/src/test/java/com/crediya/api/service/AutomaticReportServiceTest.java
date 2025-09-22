package com.crediya.api.service;

import com.crediya.usecase.generateautomaticreport.GenerateAutomaticReportUseCase;
import com.crediya.sqs.sender.SQSSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AutomaticReportServiceTest {

    @Mock
    private GenerateAutomaticReportUseCase generateAutomaticReportUseCase;

    @Mock
    private SQSSender sqsSender;

    private AutomaticReportService automaticReportService;

    @BeforeEach
    void setUp() {
        automaticReportService = new AutomaticReportService(generateAutomaticReportUseCase, sqsSender);
    }

    @Test
    void shouldGenerateAndSendReportSuccessfully() {
        String reportMessage = "{\n  \"tipo\": \"reporte_rendimiento\",\n  \"contenido\": \"Test report\"\n}";
        String messageId = "sqs-message-id-12345";

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(reportMessage));
        when(sqsSender.send(reportMessage))
                .thenReturn(Mono.just(messageId));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectNext(messageId)
                .verifyComplete();

        verify(generateAutomaticReportUseCase).generarReporteAutomatico();
        verify(sqsSender).send(reportMessage);
    }

    @Test
    void shouldHandleGenerationErrorGracefully() {
        RuntimeException generationError = new RuntimeException("Failed to generate report");

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.error(generationError));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(generateAutomaticReportUseCase).generarReporteAutomatico();
    }

    @Test
    void shouldHandleSendingErrorAfterGeneration() {
        String reportMessage = "{\n  \"tipo\": \"reporte_rendimiento\",\n  \"contenido\": \"Test report\"\n}";
        RuntimeException sendingError = new RuntimeException("SQS service unavailable");

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(reportMessage));
        when(sqsSender.send(reportMessage))
                .thenReturn(Mono.error(sendingError));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(generateAutomaticReportUseCase).generarReporteAutomatico();
        verify(sqsSender).send(reportMessage);
    }

    @Test
    void shouldHandleEmptyReportMessage() {
        String emptyMessage = "";
        String messageId = "empty-message-id";

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(emptyMessage));
        when(sqsSender.send(emptyMessage))
                .thenReturn(Mono.just(messageId));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectNext(messageId)
                .verifyComplete();
    }

    @Test
    void shouldHandleLargeReportMessage() {
        String largeMessage = "A".repeat(1000);
        String messageId = "large-message-id";

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(largeMessage));
        when(sqsSender.send(largeMessage))
                .thenReturn(Mono.just(messageId));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectNext(messageId)
                .verifyComplete();
    }

    @Test
    void shouldChainOperationsCorrectly() {
        String reportMessage = "Test report content";
        String messageId = "test-message-id";

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(reportMessage));
        when(sqsSender.send(reportMessage))
                .thenReturn(Mono.just(messageId));

        automaticReportService.generarYEnviarReporte().subscribe();

        verify(generateAutomaticReportUseCase).generarReporteAutomatico();
        verify(sqsSender).send(reportMessage);
    }

    @Test
    void shouldHandleTimeoutInGeneration() {
        RuntimeException timeoutError = new RuntimeException("Generation timeout");

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.error(timeoutError));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Generation timeout")
                )
                .verify();
    }

    @Test
    void shouldHandleTimeoutInSending() {
        String reportMessage = "Test report";
        RuntimeException timeoutError = new RuntimeException("SQS timeout");

        when(generateAutomaticReportUseCase.generarReporteAutomatico())
                .thenReturn(Mono.just(reportMessage));
        when(sqsSender.send(reportMessage))
                .thenReturn(Mono.error(timeoutError));

        Mono<String> result = automaticReportService.generarYEnviarReporte();

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("SQS timeout")
                )
                .verify();
    }
}