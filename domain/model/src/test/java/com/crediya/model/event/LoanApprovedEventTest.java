package com.crediya.model.event;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanApprovedEventTest {

    @Test
    void shouldCreateEventWithBasicData() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(new BigDecimal("5000000.00"))
                .plazo(36)
                .build();
        
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
        
        assertNotNull(event);
        assertEquals("estado_solicitud", event.getTipo());
        assertNotNull(event.getParams());
        
        assertEquals("61", event.getLoanId());
        assertEquals("APROBADO", event.getStatus());
        assertEquals(5000000.0, event.getAmount());
        assertEquals("client@cliente.com", event.getEmail());
        assertEquals(36, event.getPlazo());
        
        assertEquals("61", event.getParams().getSolicitudId());
        assertEquals("APROBADO", event.getParams().getEstado());
        assertEquals(new BigDecimal("5000000.00"), event.getParams().getMonto());
        assertEquals(36, event.getParams().getPlazo());
    }

    @Test
    void shouldHandleNullValues() {
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .email("client@cliente.com")
                .monto(null)
                .plazo(null)
                .planPago(null)
                .build();
        
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
        
        assertNotNull(event);
        assertEquals("estado_solicitud", event.getTipo());
        assertEquals("61", event.getLoanId());
        assertEquals("APROBADO", event.getStatus());
        assertNull(event.getAmount());
        assertNull(event.getPlazo());
        assertNull(event.getPlanPago());
    }

    @Test
    void shouldHandlePlanPago() {
        List<LoanApprovedEvent.PlanPagoCuota> planPago = List.of(
                LoanApprovedEvent.PlanPagoCuota.builder()
                        .numeroCuota(1)
                        .cuota(new BigDecimal("18076.20"))
                        .abonoCapital(new BigDecimal("10576.20"))
                        .interes(new BigDecimal("7500.00"))
                        .saldoRestante(new BigDecimal("489423.80"))
                        .build()
        );
        
        LoanApprovedEvent.EventParams params = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .planPago(planPago)
                .build();
        
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params)
                .build();
        
        assertNotNull(event.getPlanPago());
        assertEquals(1, event.getPlanPago().size());
        
        LoanApprovedEvent.PlanPagoCuota firstCuota = event.getPlanPago().get(0);
        assertEquals(1, firstCuota.getNumeroCuota());
        assertEquals(new BigDecimal("18076.20"), firstCuota.getCuota());
        assertEquals(new BigDecimal("10576.20"), firstCuota.getAbonoCapital());
        assertEquals(new BigDecimal("7500.00"), firstCuota.getInteres());
        assertEquals(new BigDecimal("489423.80"), firstCuota.getSaldoRestante());
    }
}
