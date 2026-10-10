package com.example.jobportal.dto;

import com.example.jobportal.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationStatusRequest(
        @NotNull(message = "Status is required") ApplicationStatus status,
        @Size(max = 1000, message = "Note must be at most 1000 characters") String note) {
}
