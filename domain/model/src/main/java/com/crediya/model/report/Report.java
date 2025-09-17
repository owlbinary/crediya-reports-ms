package com.crediya.model.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class Report {
    private final String id;
    private final Long approvedLoansCount;
    private final BigDecimal totalApprovedAmount;
    private final LocalDateTime lastUpdated;
    private final String reportType;
}
