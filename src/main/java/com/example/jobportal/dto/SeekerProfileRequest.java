package com.example.jobportal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeekerProfileRequest(
        @Size(max = 100, message = "Full name must be at most 100 characters") String fullName,
        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$", message = "Enter a valid phone number") String phone,
        @Size(max = 100, message = "Location must be at most 100 characters") String location,
        @Size(max = 150, message = "Headline must be at most 150 characters") String headline,
        @Size(max = 2000, message = "Summary must be at most 2000 characters") String summary,
        List<String> skills,
        @Size(max = 2000, message = "Education must be at most 2000 characters") String education,
        @Size(max = 2000, message = "Experience must be at most 2000 characters") String workExperience,
        @Min(value = 0, message = "Experience cannot be negative")
        @Max(value = 60, message = "Experience looks too high") Integer experienceYears,
        @Size(max = 300, message = "Resume link is too long") String resumeUrl,
        @Size(max = 300, message = "LinkedIn link is too long") String linkedinUrl) {
}
