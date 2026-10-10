package com.example.jobportal.dto;

public record AuthResponse(String token, long expiresInSeconds, UserDto user) {
}
