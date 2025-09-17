package com.crediya.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovedEvent {
    private String tipo;
    private EventParams params;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventParams {
        private String solicitudId;
        private String estado;
        private String justificacion;
        private String email;
        private BigDecimal monto;
        private Integer plazo;
        private List<PlanPagoCuota> planPago;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanPagoCuota {
        private Integer numeroCuota;
        private BigDecimal cuota;
        private BigDecimal abonoCapital;
        private BigDecimal interes;
        private BigDecimal saldoRestante;
    }

    public String getLoanId() {
        return params != null ? params.getSolicitudId() : null;
    }
    
    public Double getAmount() {
        return params != null && params.getMonto() != null ? params.getMonto().doubleValue() : null;
    }
    
    public String getStatus() {
        return params != null ? params.getEstado() : null;
    }
    
    public String getEmail() {
        return params != null ? params.getEmail() : null;
    }
    
    public Integer getPlazo() {
        return params != null ? params.getPlazo() : null;
    }
    
    public List<PlanPagoCuota> getPlanPago() {
        return params != null ? params.getPlanPago() : null;
    }
}
