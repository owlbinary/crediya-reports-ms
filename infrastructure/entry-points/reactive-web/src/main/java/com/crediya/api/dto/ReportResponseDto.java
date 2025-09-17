package com.crediya.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponseDto {
    private final String reportType;
    private final Long approvedLoansCount;
    private final BigDecimal totalApprovedAmount;
    private final LocalDateTime lastUpdated;
    private final String message;
}
