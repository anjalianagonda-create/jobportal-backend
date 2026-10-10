package com.example.jobportal.dto;

import java.util.List;

public record SeekerProfileDto(
        Long id,
        Long userId,
        String fullName,
        String email,
        String phone,
        String location,
        String headline,
        String summary,
        List<String> skills,
        String education,
        String workExperience,
        int experienceYears,
        String resumeUrl,
        String linkedinUrl) {
}
