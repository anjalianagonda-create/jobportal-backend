package com.example.jobportal.dto;

import com.example.jobportal.model.JobType;
import com.example.jobportal.model.WorkMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record JobRequest(
        @NotBlank(message = "Job title is required")
        @Size(max = 150, message = "Title must be at most 150 characters") String title,
        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description must be at most 5000 characters") String description,
        @Size(max = 2000, message = "Eligibility must be at most 2000 characters") String eligibility,
        List<String> skills,
        @NotBlank(message = "Location is required")
        @Size(max = 100, message = "Location must be at most 100 characters") String location,
        @NotNull(message = "Job type is required") JobType jobType,
        @NotNull(message = "Work mode is required") WorkMode workMode,
        @NotNull(message = "Minimum experience is required")
        @Min(value = 0, message = "Minimum experience cannot be negative")
        @Max(value = 60, message = "Minimum experience looks too high") Integer minExperience,
        @Min(value = 0, message = "Maximum experience cannot be negative")
        @Max(value = 60, message = "Maximum experience looks too high") Integer maxExperience,
        @PositiveOrZero(message = "Minimum salary cannot be negative") Long salaryMin,
        @PositiveOrZero(message = "Maximum salary cannot be negative") Long salaryMax,
        @Min(value = 1, message = "At least one opening is required")
        @Max(value = 1000, message = "Openings look too high") Integer openings,
        LocalDate deadline) {
}
