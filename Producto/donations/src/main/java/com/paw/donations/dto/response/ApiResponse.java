package com.paw.donations.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}
