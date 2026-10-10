package com.example.jobportal.dto;

import com.example.jobportal.model.Role;

import java.time.LocalDateTime;

public record UserDto(Long id, String fullName, String email, Role role, boolean active, LocalDateTime createdAt) {
}
