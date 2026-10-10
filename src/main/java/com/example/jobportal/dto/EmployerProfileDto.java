package com.example.jobportal.dto;

public record EmployerProfileDto(
        Long id,
        Long userId,
        String contactName,
        String email,
        String companyName,
        String industry,
        String companySize,
        String website,
        String location,
        String description) {
}
