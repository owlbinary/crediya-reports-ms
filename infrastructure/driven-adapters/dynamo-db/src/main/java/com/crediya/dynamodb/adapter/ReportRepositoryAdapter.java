package com.crediya.dynamodb.adapter;

import com.crediya.dynamodb.ReportEntity;
import com.crediya.model.report.Report;
import com.crediya.model.report.gateways.ReportRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Log
@Repository
public class ReportRepositoryAdapter implements ReportRepository {

    private final DynamoDbEnhancedAsyncClient enhancedAsyncClient;
    private final String tableName;
    
    public ReportRepositoryAdapter(DynamoDbEnhancedAsyncClient enhancedAsyncClient,
                                   @Value("${aws.dynamodb.table-name:crediya-dev-reportes}") String tableName) {
        this.enhancedAsyncClient = enhancedAsyncClient;
        this.tableName = tableName;
    }    @Override
    public Mono<Report> findByReportType(String reportType) {
        log.info("Buscando reporte por tipo: " + reportType);
        
        DynamoDbAsyncTable<ReportEntity> table = enhancedAsyncClient
                .table(tableName, TableSchema.fromBean(ReportEntity.class));
        
        Key key = Key.builder().partitionValue(reportType).build();
        
        return Mono.fromFuture(table.getItem(key))
                .map(this::entityToModel)
                .doOnSuccess(report -> log.info("Reporte encontrado: " + report))
                .doOnError(error -> log.severe("Error buscando reporte: " + error.getMessage()));
    }

    @Override
    public Mono<Report> save(Report report) {
        log.info("Guardando reporte: " + report);
        
        DynamoDbAsyncTable<ReportEntity> table = enhancedAsyncClient
                .table(tableName, TableSchema.fromBean(ReportEntity.class));
        
        ReportEntity entity = modelToEntity(report);
        
        return Mono.fromFuture(table.putItem(entity))
                .then(Mono.just(report))
                .doOnSuccess(savedReport -> log.info("Reporte guardado exitosamente"))
                .doOnError(error -> log.severe("Error guardando reporte: " + error.getMessage()));
    }

    @Override
    public Mono<Report> incrementCounter(String reportType) {
        log.info("Incrementando contador para tipo de reporte: " + reportType);
        
        return findByReportType(reportType)
                .switchIfEmpty(createDefaultReport(reportType))
                .map(report -> report.toBuilder()
                        .approvedLoansCount(report.getApprovedLoansCount() + 1)
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .flatMap(this::save)
                .doOnSuccess(report -> log.info("Contador incrementado exitosamente"))
                .doOnError(error -> log.severe("Error incrementando contador: " + error.getMessage()));
    }

    @Override
    public Mono<Report> incrementCounterAndAmount(String reportType, java.math.BigDecimal amount) {
        log.info("Incrementando contador y monto para tipo de reporte: " + reportType + ", monto: " + amount);
        
        return findByReportType(reportType)
                .switchIfEmpty(createDefaultReport(reportType))
                .map(report -> report.toBuilder()
                        .approvedLoansCount(report.getApprovedLoansCount() + 1)
                        .totalApprovedAmount(report.getTotalApprovedAmount().add(amount))
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .flatMap(this::save)
                .doOnSuccess(report -> log.info("Contador y monto incrementados exitosamente"))
                .doOnError(error -> log.severe("Error incrementando contador y monto: " + error.getMessage()));
    }

    private Mono<Report> createDefaultReport(String reportType) {
        log.info("Creando reporte por defecto para tipo: " + reportType);
        
        return Mono.just(Report.builder()
                .id(reportType)
                .reportType(reportType)
                .approvedLoansCount(0L)
                .totalApprovedAmount(java.math.BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build());
    }

    private Report entityToModel(ReportEntity entity) {
        if (entity == null) {
            return null;
        }
        
        LocalDateTime lastUpdated = entity.getLastUpdated() != null 
                ? LocalDateTime.ofInstant(entity.getLastUpdated(), ZoneOffset.UTC)
                : null;
        
        return Report.builder()
                .id(entity.getId())
                .reportType(entity.getReportType())
                .approvedLoansCount(entity.getApprovedLoansCount())
                .totalApprovedAmount(entity.getTotalApprovedAmount() != null ? entity.getTotalApprovedAmount() : java.math.BigDecimal.ZERO)
                .lastUpdated(lastUpdated)
                .build();
    }

    private ReportEntity modelToEntity(Report report) {
        ReportEntity entity = new ReportEntity();
        entity.setId(report.getId());
        entity.setReportType(report.getReportType());
        entity.setApprovedLoansCount(report.getApprovedLoansCount());
        entity.setTotalApprovedAmount(report.getTotalApprovedAmount());
        
        if (report.getLastUpdated() != null) {
            entity.setLastUpdated(report.getLastUpdated().toInstant(ZoneOffset.UTC));
        }
        
        return entity;
    }
}
