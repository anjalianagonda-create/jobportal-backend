package com.example.jobportal.dto;

import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.JobStatus;

import java.time.LocalDateTime;

public record ApplicationDto(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        String jobLocation,
        JobStatus jobStatus,
        ApplicationStatus status,
        String coverLetter,
        String employerNote,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        /** Only populated for the employer's view. */
        SeekerProfileDto applicant) {
}
