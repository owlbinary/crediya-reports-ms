package com.crediya.api;

import com.crediya.model.report.Report;
import com.crediya.usecase.getreportcounter.GetReportCounterUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, RouterRestTest.TestConfig.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public GetReportCounterUseCase getReportCounterUseCase() {
            GetReportCounterUseCase mock = mock(GetReportCounterUseCase.class);
            Report report = Report.builder()
                    .id("1")
                    .approvedLoansCount(10L)
                    .totalApprovedAmount(BigDecimal.valueOf(50000000.00))
                    .lastUpdated(LocalDateTime.now())
                    .reportType("APPROVED_LOANS")
                    .build();
            when(mock.obtenerContadorPrestamosAprobados()).thenReturn(Mono.just(report));
            return mock;
        }
    }

    @Test
    void contextLoads() {
        org.junit.jupiter.api.Assertions.assertNotNull(webTestClient);
    }
    
    @Test
    void shouldReturnApprovedLoansReport() {
        webTestClient.get()
                .uri("/api/v1/reportes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reportType").isEqualTo("APPROVED_LOANS")
                .jsonPath("$.approvedLoansCount").isEqualTo(10)
                .jsonPath("$.totalApprovedAmount").isEqualTo(50000000.00)
                .jsonPath("$.message").isEqualTo("Cantidad total de préstamos aprobados obtenida exitosamente");
    }
}
