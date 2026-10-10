package com.example.jobportal.dto;

import com.example.jobportal.model.JobStatus;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.WorkMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobDto(
        Long id,
        String title,
        String description,
        String eligibility,
        List<String> skills,
        String location,
        JobType jobType,
        WorkMode workMode,
        int minExperience,
        Integer maxExperience,
        Long salaryMin,
        Long salaryMax,
        int openings,
        LocalDate deadline,
        JobStatus status,
        LocalDateTime createdAt,
        Long employerId,
        String companyName,
        String companyWebsite,
        String companyIndustry,
        String companyDescription,
        Long applicantCount) {
}
