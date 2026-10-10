package com.example.jobportal.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Uniform error payload returned by every failing API call. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {
}
