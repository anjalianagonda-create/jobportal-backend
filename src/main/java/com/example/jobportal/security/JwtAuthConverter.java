package com.example.jobportal.security;

import com.example.jobportal.model.User;
import com.example.jobportal.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Turns a verified JWT into an authenticated principal. The account is re-checked in the database on every
 * request, so deactivating a user (admin action) takes effect immediately even for tokens already issued.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository users;

    public JwtAuthConverter(UserRepository users) {
        this.users = users;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long id;
        try {
            id = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("Invalid token subject");
        }
        User user = users.findById(id).orElseThrow(() -> new BadCredentialsException("Account no longer exists"));
        if (!user.isActive()) {
            throw new DisabledException("Account has been deactivated");
        }
        return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                String.valueOf(user.getId()));
    }
}
