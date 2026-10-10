package com.example.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployerProfileRequest(
        @Size(max = 100, message = "Contact name must be at most 100 characters") String contactName,
        @NotBlank(message = "Company name is required")
        @Size(max = 150, message = "Company name must be at most 150 characters") String companyName,
        @Size(max = 100, message = "Industry must be at most 100 characters") String industry,
        @Size(max = 50, message = "Company size must be at most 50 characters") String companySize,
        @Size(max = 300, message = "Website is too long") String website,
        @Size(max = 100, message = "Location must be at most 100 characters") String location,
        @Size(max = 2000, message = "Description must be at most 2000 characters") String description) {
}
