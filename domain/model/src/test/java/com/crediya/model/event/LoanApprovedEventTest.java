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

    @Test
    void shouldReturnNullWhenParamsIsNull() {
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(null)
                .build();
        
        assertNotNull(event);
        assertEquals("estado_solicitud", event.getTipo());
        assertNull(event.getParams());
        
        assertNull(event.getLoanId());
        assertNull(event.getAmount());
        assertNull(event.getStatus());
        assertNull(event.getEmail());
        assertNull(event.getPlazo());
        assertNull(event.getPlanPago());
    }

    @Test
    void shouldCreateEventWithNoArgsConstructor() {
        LoanApprovedEvent event = new LoanApprovedEvent();
        
        assertNotNull(event);
        assertNull(event.getTipo());
        assertNull(event.getParams());
        
        assertNull(event.getLoanId());
        assertNull(event.getAmount());
        assertNull(event.getStatus());
        assertNull(event.getEmail());
        assertNull(event.getPlazo());
        assertNull(event.getPlanPago());
    }

    @Test
    void shouldCreateEventWithAllArgsConstructor() {
        LoanApprovedEvent.EventParams params = new LoanApprovedEvent.EventParams(
                "123", "APROBADO", "Aprobado automáticamente", 
                "test@test.com", new BigDecimal("1000000.00"), 24, null);
        
        LoanApprovedEvent event = new LoanApprovedEvent("actualizar_estado", params);
        
        assertNotNull(event);
        assertEquals("actualizar_estado", event.getTipo());
        assertNotNull(event.getParams());
        assertEquals("123", event.getLoanId());
        assertEquals(1000000.0, event.getAmount());
        assertEquals("APROBADO", event.getStatus());
        assertEquals("test@test.com", event.getEmail());
        assertEquals(24, event.getPlazo());
        assertNull(event.getPlanPago());
    }

    @Test
    void shouldCreateEventParamsWithNoArgsConstructor() {
        LoanApprovedEvent.EventParams params = new LoanApprovedEvent.EventParams();
        
        assertNotNull(params);
        assertNull(params.getSolicitudId());
        assertNull(params.getEstado());
        assertNull(params.getJustificacion());
        assertNull(params.getEmail());
        assertNull(params.getMonto());
        assertNull(params.getPlazo());
        assertNull(params.getPlanPago());
    }

    @Test
    void shouldCreatePlanPagoCuotaWithNoArgsConstructor() {
        LoanApprovedEvent.PlanPagoCuota cuota = new LoanApprovedEvent.PlanPagoCuota();
        
        assertNotNull(cuota);
        assertNull(cuota.getNumeroCuota());
        assertNull(cuota.getCuota());
        assertNull(cuota.getAbonoCapital());
        assertNull(cuota.getInteres());
        assertNull(cuota.getSaldoRestante());
    }

    @Test
    void shouldCreatePlanPagoCuotaWithAllArgsConstructor() {
        LoanApprovedEvent.PlanPagoCuota cuota = new LoanApprovedEvent.PlanPagoCuota(
                1, new BigDecimal("5000.00"), new BigDecimal("4000.00"), 
                new BigDecimal("1000.00"), new BigDecimal("96000.00"));
        
        assertNotNull(cuota);
        assertEquals(1, cuota.getNumeroCuota());
        assertEquals(new BigDecimal("5000.00"), cuota.getCuota());
        assertEquals(new BigDecimal("4000.00"), cuota.getAbonoCapital());
        assertEquals(new BigDecimal("1000.00"), cuota.getInteres());
        assertEquals(new BigDecimal("96000.00"), cuota.getSaldoRestante());
    }

    @Test
    void shouldTestEqualsAndHashCodeForLoanApprovedEvent() {
        LoanApprovedEvent.EventParams params1 = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .build();
        
        LoanApprovedEvent.EventParams params2 = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .build();
        
        LoanApprovedEvent event1 = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params1)
                .build();
        
        LoanApprovedEvent event2 = LoanApprovedEvent.builder()
                .tipo("estado_solicitud")
                .params(params2)
                .build();
        
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotNull(event1.toString());
        assertTrue(event1.toString().contains("LoanApprovedEvent"));
    }

    @Test
    void shouldTestEqualsAndHashCodeForEventParams() {
        LoanApprovedEvent.EventParams params1 = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .monto(new BigDecimal("1000.00"))
                .build();
        
        LoanApprovedEvent.EventParams params2 = LoanApprovedEvent.EventParams.builder()
                .solicitudId("61")
                .estado("APROBADO")
                .monto(new BigDecimal("1000.00"))
                .build();
        
        assertEquals(params1, params2);
        assertEquals(params1.hashCode(), params2.hashCode());
        assertNotNull(params1.toString());
        assertTrue(params1.toString().contains("EventParams"));
    }

    @Test
    void shouldTestEqualsAndHashCodeForPlanPagoCuota() {
        LoanApprovedEvent.PlanPagoCuota cuota1 = LoanApprovedEvent.PlanPagoCuota.builder()
                .numeroCuota(1)
                .cuota(new BigDecimal("5000.00"))
                .build();
        
        LoanApprovedEvent.PlanPagoCuota cuota2 = LoanApprovedEvent.PlanPagoCuota.builder()
                .numeroCuota(1)
                .cuota(new BigDecimal("5000.00"))
                .build();
        
        assertEquals(cuota1, cuota2);
        assertEquals(cuota1.hashCode(), cuota2.hashCode());
        assertNotNull(cuota1.toString());
        assertTrue(cuota1.toString().contains("PlanPagoCuota"));
    }

    @Test
    void shouldHandleSettersForLoanApprovedEvent() {
        LoanApprovedEvent event = new LoanApprovedEvent();
        LoanApprovedEvent.EventParams params = new LoanApprovedEvent.EventParams();
        
        event.setTipo("test_tipo");
        event.setParams(params);
        
        assertEquals("test_tipo", event.getTipo());
        assertEquals(params, event.getParams());
    }

    @Test
    void shouldHandleSettersForEventParams() {
        LoanApprovedEvent.EventParams params = new LoanApprovedEvent.EventParams();
        
        params.setSolicitudId("123");
        params.setEstado("APROBADO");
        params.setJustificacion("Test justification");
        params.setEmail("test@test.com");
        params.setMonto(new BigDecimal("2000.00"));
        params.setPlazo(12);
        params.setPlanPago(List.of());
        
        assertEquals("123", params.getSolicitudId());
        assertEquals("APROBADO", params.getEstado());
        assertEquals("Test justification", params.getJustificacion());
        assertEquals("test@test.com", params.getEmail());
        assertEquals(new BigDecimal("2000.00"), params.getMonto());
        assertEquals(12, params.getPlazo());
        assertEquals(List.of(), params.getPlanPago());
    }

    @Test
    void shouldHandleSettersForPlanPagoCuota() {
        LoanApprovedEvent.PlanPagoCuota cuota = new LoanApprovedEvent.PlanPagoCuota();
        
        cuota.setNumeroCuota(2);
        cuota.setCuota(new BigDecimal("3000.00"));
        cuota.setAbonoCapital(new BigDecimal("2500.00"));
        cuota.setInteres(new BigDecimal("500.00"));
        cuota.setSaldoRestante(new BigDecimal("97000.00"));
        
        assertEquals(2, cuota.getNumeroCuota());
        assertEquals(new BigDecimal("3000.00"), cuota.getCuota());
        assertEquals(new BigDecimal("2500.00"), cuota.getAbonoCapital());
        assertEquals(new BigDecimal("500.00"), cuota.getInteres());
        assertEquals(new BigDecimal("97000.00"), cuota.getSaldoRestante());
    }
}
