package com.crediya.usecase.eventos;

import com.crediya.model.event.LoanApprovedEvent;
import com.crediya.usecase.incrementreportcounter.IncrementReportCounterUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProcesarSolicitudAprobadaUseCase {
    
    private final IncrementReportCounterUseCase incrementreportcounterUseCase;
    
    public Mono<Void> procesar(LoanApprovedEvent event) {
        return incrementreportcounterUseCase.incrementarContador(event);
    }
}
