package com.crediya.usecase.incrementreportcounter;

import com.crediya.model.event.LoanApprovedEvent;
import com.crediya.model.report.gateways.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Log
@RequiredArgsConstructor
public class IncrementReportCounterUseCase {
    
    private static final String APPROVED_LOANS_REPORT_TYPE = "APPROVED_LOANS";
    private final ReportRepository reportRepository;

    public Mono<Void> incrementarContador(LoanApprovedEvent event) {
        log.info("Procesando evento de préstamo aprobado: " + event.getLoanId() + " con monto: " + event.getAmount());
        
        BigDecimal amount = event.getAmount() != null ? BigDecimal.valueOf(event.getAmount()) : BigDecimal.ZERO;
        
        return reportRepository.incrementCounterAndAmount(APPROVED_LOANS_REPORT_TYPE, amount)
                .doOnSuccess(report -> log.info("Contador y monto incrementados exitosamente. Nuevo contador: " + 
                    report.getApprovedLoansCount() + ", Monto total: " + report.getTotalApprovedAmount()))
                .doOnError(error -> log.severe("Error incrementando contador y monto: " + error.getMessage()))
                .then()
                .onErrorResume(error -> {
                    log.severe("Fallo al incrementar contador para evento: " + event.getLoanId() + ". Error: " + error.getMessage());
                    return Mono.empty();
                });
    }
}
