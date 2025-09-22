package com.crediya.config;

import com.crediya.model.report.gateways.ReportRepository;
import com.crediya.usecase.eventos.ProcesarSolicitudAprobadaUseCase;
import com.crediya.usecase.generateautomaticreport.GenerateAutomaticReportUseCase;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcesarSolicitudAprobadaUseCase procesarSolicitudAprobadaUseCase(
            IncrementReportCounterUseCase incrementreportcounterUseCase) {
        return new ProcesarSolicitudAprobadaUseCase(incrementreportcounterUseCase);
    }
    
    @Bean
    public GenerateAutomaticReportUseCase generateAutomaticReportUseCase(
            ReportRepository reportRepository,
            @Value("${app.admin.emails:admin@crediya.com}") String adminEmails) {
        return new GenerateAutomaticReportUseCase(reportRepository, adminEmails);
    }
}
