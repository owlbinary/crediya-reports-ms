package com.crediya.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDto {
    private final String error;
    private final String message;
    private final int status;
    private final String timestamp;
}
