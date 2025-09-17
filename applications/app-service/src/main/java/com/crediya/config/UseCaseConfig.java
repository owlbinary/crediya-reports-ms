package com.crediya.config;

import com.crediya.usecase.eventos.ProcesarSolicitudAprobadaUseCase;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcesarSolicitudAprobadaUseCase procesarSolicitudAprobadaUseCase(
            IncrementReportCounterUseCase incrementreportcounterUseCase) {
        return new ProcesarSolicitudAprobadaUseCase(incrementreportcounterUseCase);
    }
}
