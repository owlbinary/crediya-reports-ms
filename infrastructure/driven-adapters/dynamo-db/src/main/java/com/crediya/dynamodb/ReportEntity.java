package com.crediya.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ReportEntity {

    private String id;
    private Long approvedLoansCount;
    private BigDecimal totalApprovedAmount;
    private Instant lastUpdated;
    private String reportType;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    @DynamoDbAttribute("approved_loans_count")
    public Long getApprovedLoansCount() {
        return approvedLoansCount;
    }

    @DynamoDbAttribute("total_approved_amount")
    public BigDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    @DynamoDbAttribute("last_updated")
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @DynamoDbAttribute("report_type")
    public String getReportType() {
        return reportType;
    }
}
