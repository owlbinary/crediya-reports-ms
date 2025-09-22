package com.crediya.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReportResponseDtoTest {

    @Test
    void shouldCreateReportResponseDtoWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("1500000.75");
        Long count = 42L;

        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType("APPROVED_LOANS")
                .approvedLoansCount(count)
                .totalApprovedAmount(amount)
                .lastUpdated(now)
                .message("Report generated successfully")
                .build();

        assertEquals("APPROVED_LOANS", dto.getReportType());
        assertEquals(count, dto.getApprovedLoansCount());
        assertEquals(amount, dto.getTotalApprovedAmount());
        assertEquals(now, dto.getLastUpdated());
        assertEquals("Report generated successfully", dto.getMessage());
    }

    @Test
    void shouldCreateReportResponseDtoWithNullValues() {
        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType(null)
                .approvedLoansCount(null)
                .totalApprovedAmount(null)
                .lastUpdated(null)
                .message(null)
                .build();

        assertNull(dto.getReportType());
        assertNull(dto.getApprovedLoansCount());
        assertNull(dto.getTotalApprovedAmount());
        assertNull(dto.getLastUpdated());
        assertNull(dto.getMessage());
    }

    @Test
    void shouldCreateReportResponseDtoWithZeroValues() {
        LocalDateTime now = LocalDateTime.now();

        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType("EMPTY_REPORT")
                .approvedLoansCount(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(now)
                .message("No data available")
                .build();

        assertEquals("EMPTY_REPORT", dto.getReportType());
        assertEquals(0L, dto.getApprovedLoansCount());
        assertEquals(BigDecimal.ZERO, dto.getTotalApprovedAmount());
        assertEquals(now, dto.getLastUpdated());
        assertEquals("No data available", dto.getMessage());
    }

    @Test
    void shouldCreateReportResponseDtoWithLargeValues() {
        LocalDateTime futureDate = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        BigDecimal largeAmount = new BigDecimal("999999999999.99");
        Long largeCount = Long.MAX_VALUE;

        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType("LARGE_DATASET")
                .approvedLoansCount(largeCount)
                .totalApprovedAmount(largeAmount)
                .lastUpdated(futureDate)
                .message("Large dataset processed")
                .build();

        assertEquals("LARGE_DATASET", dto.getReportType());
        assertEquals(largeCount, dto.getApprovedLoansCount());
        assertEquals(largeAmount, dto.getTotalApprovedAmount());
        assertEquals(futureDate, dto.getLastUpdated());
        assertEquals("Large dataset processed", dto.getMessage());
    }

    @Test
    void shouldSupportEquality() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("100000.00");

        ReportResponseDto dto1 = ReportResponseDto.builder()
                .reportType("TEST")
                .approvedLoansCount(10L)
                .totalApprovedAmount(amount)
                .lastUpdated(now)
                .message("Test message")
                .build();

        ReportResponseDto dto2 = ReportResponseDto.builder()
                .reportType("TEST")
                .approvedLoansCount(10L)
                .totalApprovedAmount(amount)
                .lastUpdated(now)
                .message("Test message")
                .build();

        ReportResponseDto dto3 = ReportResponseDto.builder()
                .reportType("DIFFERENT")
                .approvedLoansCount(20L)
                .totalApprovedAmount(amount)
                .lastUpdated(now)
                .message("Different message")
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void shouldProvideToStringRepresentation() {
        LocalDateTime now = LocalDateTime.now();

        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType("PERFORMANCE")
                .approvedLoansCount(25L)
                .totalApprovedAmount(new BigDecimal("500000.50"))
                .lastUpdated(now)
                .message("Performance report")
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("reportType=PERFORMANCE"));
        assertTrue(toString.contains("approvedLoansCount=25"));
        assertTrue(toString.contains("totalApprovedAmount=500000.50"));
        assertTrue(toString.contains("message=Performance report"));
    }

    @Test
    void shouldHandlePartialBuilder() {
        ReportResponseDto dto = ReportResponseDto.builder()
                .reportType("PARTIAL")
                .approvedLoansCount(5L)
                .build();

        assertEquals("PARTIAL", dto.getReportType());
        assertEquals(5L, dto.getApprovedLoansCount());
        assertNull(dto.getTotalApprovedAmount());
        assertNull(dto.getLastUpdated());
        assertNull(dto.getMessage());
    }
}