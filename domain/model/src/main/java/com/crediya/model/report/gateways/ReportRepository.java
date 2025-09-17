package com.crediya.model.report.gateways;

import com.crediya.model.report.Report;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface ReportRepository {
    Mono<Report> findByReportType(String reportType);
    Mono<Report> save(Report report);
    Mono<Report> incrementCounter(String reportType);
    Mono<Report> incrementCounterAndAmount(String reportType, BigDecimal amount);
}
