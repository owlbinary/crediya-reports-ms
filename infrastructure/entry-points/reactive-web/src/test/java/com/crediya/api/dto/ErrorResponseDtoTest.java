package com.crediya.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseDtoTest {

    @Test
    void shouldCreateErrorResponseDtoWithBuilder() {
        String error = "VALIDATION_ERROR";
        String message = "Invalid request parameters";
        int status = 400;
        String timestamp = "2025-09-21T22:30:00";

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(timestamp)
                .build();

        assertEquals(error, dto.getError());
        assertEquals(message, dto.getMessage());
        assertEquals(status, dto.getStatus());
        assertEquals(timestamp, dto.getTimestamp());
    }

    @Test
    void shouldCreateErrorResponseDtoWithNullValues() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error(null)
                .message(null)
                .status(0)
                .timestamp(null)
                .build();

        assertNull(dto.getError());
        assertNull(dto.getMessage());
        assertEquals(0, dto.getStatus());
        assertNull(dto.getTimestamp());
    }

    @Test
    void shouldCreateInternalServerError() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .status(500)
                .timestamp("2025-09-21T22:30:15")
                .build();

        assertEquals("INTERNAL_SERVER_ERROR", dto.getError());
        assertEquals("An unexpected error occurred", dto.getMessage());
        assertEquals(500, dto.getStatus());
        assertEquals("2025-09-21T22:30:15", dto.getTimestamp());
    }

    @Test
    void shouldCreateNotFoundError() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("NOT_FOUND")
                .message("Resource not found")
                .status(404)
                .timestamp("2025-09-21T22:30:30")
                .build();

        assertEquals("NOT_FOUND", dto.getError());
        assertEquals("Resource not found", dto.getMessage());
        assertEquals(404, dto.getStatus());
        assertEquals("2025-09-21T22:30:30", dto.getTimestamp());
    }

    @Test
    void shouldCreateUnauthorizedError() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("UNAUTHORIZED")
                .message("Authentication required")
                .status(401)
                .timestamp("2025-09-21T22:30:45")
                .build();

        assertEquals("UNAUTHORIZED", dto.getError());
        assertEquals("Authentication required", dto.getMessage());
        assertEquals(401, dto.getStatus());
        assertEquals("2025-09-21T22:30:45", dto.getTimestamp());
    }

    @Test
    void shouldSupportEquality() {
        ErrorResponseDto dto1 = ErrorResponseDto.builder()
                .error("BAD_REQUEST")
                .message("Invalid input")
                .status(400)
                .timestamp("2025-09-21T22:31:00")
                .build();

        ErrorResponseDto dto2 = ErrorResponseDto.builder()
                .error("BAD_REQUEST")
                .message("Invalid input")
                .status(400)
                .timestamp("2025-09-21T22:31:00")
                .build();

        ErrorResponseDto dto3 = ErrorResponseDto.builder()
                .error("FORBIDDEN")
                .message("Access denied")
                .status(403)
                .timestamp("2025-09-21T22:31:15")
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void shouldProvideToStringRepresentation() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("TIMEOUT")
                .message("Request timeout")
                .status(408)
                .timestamp("2025-09-21T22:31:30")
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("error=TIMEOUT"));
        assertTrue(toString.contains("message=Request timeout"));
        assertTrue(toString.contains("status=408"));
        assertTrue(toString.contains("timestamp=2025-09-21T22:31:30"));
    }

    @Test
    void shouldHandlePartialBuilder() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("PARTIAL_ERROR")
                .status(422)
                .build();

        assertEquals("PARTIAL_ERROR", dto.getError());
        assertEquals(422, dto.getStatus());
        assertNull(dto.getMessage());
        assertNull(dto.getTimestamp());
    }

    @Test
    void shouldHandleEmptyStrings() {
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("")
                .message("")
                .status(200)
                .timestamp("")
                .build();

        assertEquals("", dto.getError());
        assertEquals("", dto.getMessage());
        assertEquals(200, dto.getStatus());
        assertEquals("", dto.getTimestamp());
    }
}