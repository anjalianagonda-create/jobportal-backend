package com.example.jobportal.service;

import com.example.jobportal.dto.AuthResponse;
import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.dto.LoginRequest;
import com.example.jobportal.dto.RegisterRequest;
import com.example.jobportal.dto.UserDto;
import com.example.jobportal.exception.BadRequestException;
import com.example.jobportal.exception.ConflictException;
import com.example.jobportal.exception.ForbiddenException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.exception.UnauthorizedException;
import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.JobSeekerProfile;
import com.example.jobportal.model.Role;
import com.example.jobportal.model.User;
import com.example.jobportal.repository.EmployerProfileRepository;
import com.example.jobportal.repository.JobSeekerProfileRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final JobSeekerProfileRepository seekers;
    private final EmployerProfileRepository employers;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, JobSeekerProfileRepository seekers,
                       EmployerProfileRepository employers, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.seekers = seekers;
        this.employers = employers;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (req.role() == Role.ADMIN) {
            throw new BadRequestException("Administrator accounts cannot be self-registered");
        }
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        if (req.role() == Role.EMPLOYER && (req.companyName() == null || req.companyName().isBlank())) {
            throw new BadRequestException("Company name is required for employer accounts");
        }

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(req.password()));
        user.setRole(req.role());
        user = users.save(user);

        if (req.role() == Role.EMPLOYER) {
            employers.save(new EmployerProfile(user, req.companyName().trim()));
        } else {
            seekers.save(new JobSeekerProfile(user));
        }
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isActive()) {
            throw new ForbiddenException("This account has been deactivated. Please contact the administrator.");
        }
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return users.findById(userId).map(DtoMapper::user)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(jwt.generateToken(user), jwt.getExpiresInSeconds(), DtoMapper.user(user));
    }
}
