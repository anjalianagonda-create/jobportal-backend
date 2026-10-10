package com.example.jobportal.security;

import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() { }

    /** The authenticated user's id (JWT subject). */
    public static Long id(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
