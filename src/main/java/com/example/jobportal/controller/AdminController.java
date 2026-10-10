package com.example.jobportal.controller;

import com.example.jobportal.dto.ActiveRequest;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.JobStatusRequest;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserDto;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.model.Role;
import com.example.jobportal.security.CurrentUser;
import com.example.jobportal.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return admin.stats();
    }

    @GetMapping("/users")
    public PageResponse<UserDto> users(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) Role role,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return admin.listUsers(q, role, page, size);
    }

    @PatchMapping("/users/{id}/active")
    public UserDto setActive(Authentication auth, @PathVariable Long id, @RequestBody ActiveRequest request) {
        return admin.setUserActive(CurrentUser.id(auth), id, request.active());
    }

    @GetMapping("/jobs")
    public PageResponse<JobDto> jobs(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) JobStatus status,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return admin.listJobs(q, status, page, size);
    }

    @PatchMapping("/jobs/{id}/status")
    public JobDto setJobStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        return admin.setJobStatus(id, request.status());
    }
}
