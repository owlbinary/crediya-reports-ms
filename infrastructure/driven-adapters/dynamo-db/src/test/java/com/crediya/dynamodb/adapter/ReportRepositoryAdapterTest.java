package com.crediya.dynamodb.adapter;

import com.crediya.dynamodb.ReportEntity;
import com.crediya.model.report.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReportRepositoryAdapterTest {

    @Mock
    private DynamoDbEnhancedAsyncClient enhancedAsyncClient;

    @Mock
    private DynamoDbAsyncTable<ReportEntity> dynamoTable;

    private ReportRepositoryAdapter repositoryAdapter;
    private final String tableName = "crediya-test-reportes";

    @BeforeEach
    void setUp() {
        repositoryAdapter = new ReportRepositoryAdapter(enhancedAsyncClient, tableName);
        
        when(enhancedAsyncClient.table(eq(tableName), any(TableSchema.class)))
                .thenReturn(dynamoTable);
    }

    @Test
    void shouldFindReportByTypeSuccessfully() {
        String reportType = "APPROVED_LOANS";
        ReportEntity entity = createReportEntity(reportType, 15L, new BigDecimal("75000000.50"));
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(entity));

        Mono<Report> result = repositoryAdapter.findByReportType(reportType);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getReportType().equals(reportType) &&
                    report.getApprovedLoansCount().equals(15L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("75000000.50"))
                )
                .verifyComplete();

        verify(enhancedAsyncClient).table(tableName, TableSchema.fromBean(ReportEntity.class));
        verify(dynamoTable).getItem(expectedKey);
    }

    @Test
    void shouldReturnNullWhenReportNotFound() {
        String reportType = "NON_EXISTENT";
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.findByReportType(reportType);

        StepVerifier.create(result)
                .expectNext()
                .verifyComplete();

        verify(dynamoTable).getItem(expectedKey);
    }

    @Test
    void shouldHandleDynamoDbErrorOnFind() {
        String reportType = "APPROVED_LOANS";
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        RuntimeException dynamoError = new RuntimeException("DynamoDB service unavailable");
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.failedFuture(dynamoError));

        Mono<Report> result = repositoryAdapter.findByReportType(reportType);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldSaveReportSuccessfully() {
        Report report = createReport("APPROVED_LOANS", 25L, new BigDecimal("125000000.75"));
        
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.save(report);

        StepVerifier.create(result)
                .expectNext(report)
                .verifyComplete();

        verify(dynamoTable).putItem(any(ReportEntity.class));
    }

    @Test
    void shouldHandleDynamoDbErrorOnSave() {
        Report report = createReport("APPROVED_LOANS", 10L, new BigDecimal("50000000.00"));
        RuntimeException saveError = new RuntimeException("Failed to save to DynamoDB");
        
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.failedFuture(saveError));

        Mono<Report> result = repositoryAdapter.save(report);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldIncrementCounterForExistingReport() {
        String reportType = "APPROVED_LOANS";
        ReportEntity existingEntity = createReportEntity(reportType, 5L, new BigDecimal("25000000.00"));
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(existingEntity));
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.incrementCounter(reportType);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(6L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("25000000.00"))
                )
                .verifyComplete();

        verify(dynamoTable).getItem(expectedKey);
        verify(dynamoTable).putItem(any(ReportEntity.class));
    }

    @Test
    void shouldCreateDefaultReportWhenIncrementingNonExistent() {
        String reportType = "NEW_REPORT_TYPE";
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.incrementCounter(reportType);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(1L) &&
                    report.getTotalApprovedAmount().equals(BigDecimal.ZERO) &&
                    report.getReportType().equals(reportType)
                )
                .verifyComplete();

        verify(dynamoTable).putItem(any(ReportEntity.class));
    }

    @Test
    void shouldIncrementCounterAndAmountSuccessfully() {
        String reportType = "APPROVED_LOANS";
        BigDecimal incrementAmount = new BigDecimal("5000000.00");
        ReportEntity existingEntity = createReportEntity(reportType, 10L, new BigDecimal("50000000.00"));
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(existingEntity));
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.incrementCounterAndAmount(reportType, incrementAmount);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(11L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("55000000.00"))
                )
                .verifyComplete();

        verify(dynamoTable).putItem(any(ReportEntity.class));
    }

    @Test
    void shouldCreateDefaultReportForCounterAndAmountWhenNotExists() {
        String reportType = "NEW_LOANS";
        BigDecimal incrementAmount = new BigDecimal("3000000.00");
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(dynamoTable.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.incrementCounterAndAmount(reportType, incrementAmount);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getApprovedLoansCount().equals(1L) &&
                    report.getTotalApprovedAmount().equals(new BigDecimal("3000000.00"))
                )
                .verifyComplete();

        verify(dynamoTable).putItem(any(ReportEntity.class));
    }

    @Test
    void shouldHandleNullEntityInEntityToModel() {
        String reportType = "TEST_REPORT";
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = repositoryAdapter.findByReportType(reportType);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldConvertEntityWithNullAmountCorrectly() {
        String reportType = "NULL_AMOUNT_TEST";
        ReportEntity entityWithNullAmount = new ReportEntity();
        entityWithNullAmount.setId(reportType);
        entityWithNullAmount.setReportType(reportType);
        entityWithNullAmount.setApprovedLoansCount(5L);
        entityWithNullAmount.setTotalApprovedAmount(null);
        entityWithNullAmount.setLastUpdated(Instant.now());
        
        Key expectedKey = Key.builder().partitionValue(reportType).build();
        
        when(dynamoTable.getItem(expectedKey))
                .thenReturn(CompletableFuture.completedFuture(entityWithNullAmount));

        Mono<Report> result = repositoryAdapter.findByReportType(reportType);

        StepVerifier.create(result)
                .expectNextMatches(report -> 
                    report.getTotalApprovedAmount().equals(BigDecimal.ZERO)
                )
                .verifyComplete();
    }

    private ReportEntity createReportEntity(String reportType, Long count, BigDecimal amount) {
        ReportEntity entity = new ReportEntity();
        entity.setId(reportType);
        entity.setReportType(reportType);
        entity.setApprovedLoansCount(count);
        entity.setTotalApprovedAmount(amount);
        entity.setLastUpdated(Instant.now());
        return entity;
    }

    private Report createReport(String reportType, Long count, BigDecimal amount) {
        return Report.builder()
                .id(reportType)
                .reportType(reportType)
                .approvedLoansCount(count)
                .totalApprovedAmount(amount)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
