package com.paw.campaigns.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}
