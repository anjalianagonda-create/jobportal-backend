package com.example.jobportal.dto;

import com.example.jobportal.model.JobStatus;
import jakarta.validation.constraints.NotNull;

public record JobStatusRequest(@NotNull(message = "Status is required") JobStatus status) {
}
