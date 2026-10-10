package com.example.jobportal.dto;

import jakarta.validation.constraints.Size;

public record ApplyRequest(
        @Size(max = 3000, message = "Cover letter must be at most 3000 characters") String coverLetter) {
}
