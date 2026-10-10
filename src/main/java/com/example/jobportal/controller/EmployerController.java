package com.example.jobportal.controller;

import com.example.jobportal.dto.ApplicationDto;
import com.example.jobportal.dto.ApplicationStatusRequest;
import com.example.jobportal.dto.EmployerProfileDto;
import com.example.jobportal.dto.EmployerProfileRequest;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.JobStatusRequest;
import com.example.jobportal.security.CurrentUser;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.EmployerService;
import com.example.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    private final EmployerService employers;
    private final JobService jobs;
    private final ApplicationService applications;

    public EmployerController(EmployerService employers, JobService jobs, ApplicationService applications) {
        this.employers = employers;
        this.jobs = jobs;
        this.applications = applications;
    }

    @GetMapping("/stats")
    public Map<String, Long> stats(Authentication auth) {
        return employers.stats(CurrentUser.id(auth));
    }

    @GetMapping("/profile")
    public EmployerProfileDto profile(Authentication auth) {
        return employers.getProfile(CurrentUser.id(auth));
    }

    @PutMapping("/profile")
    public EmployerProfileDto updateProfile(Authentication auth, @Valid @RequestBody EmployerProfileRequest request) {
        return employers.updateProfile(CurrentUser.id(auth), request);
    }

    @GetMapping("/jobs")
    public List<JobDto> myJobs(Authentication auth) {
        return jobs.listForEmployer(CurrentUser.id(auth));
    }

    @GetMapping("/jobs/{id}")
    public JobDto job(Authentication auth, @PathVariable Long id) {
        return jobs.getOwned(CurrentUser.id(auth), id);
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobDto create(Authentication auth, @Valid @RequestBody JobRequest request) {
        return jobs.create(CurrentUser.id(auth), request);
    }

    @PutMapping("/jobs/{id}")
    public JobDto update(Authentication auth, @PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobs.update(CurrentUser.id(auth), id, request);
    }

    @PatchMapping("/jobs/{id}/status")
    public JobDto changeStatus(Authentication auth, @PathVariable Long id,
                               @Valid @RequestBody JobStatusRequest request) {
        return jobs.changeStatus(CurrentUser.id(auth), id, request.status());
    }

    @GetMapping("/jobs/{id}/applications")
    public List<ApplicationDto> applicants(Authentication auth, @PathVariable Long id) {
        return applications.listForJob(CurrentUser.id(auth), id);
    }

    @PatchMapping("/applications/{id}/status")
    public ApplicationDto updateApplicationStatus(Authentication auth, @PathVariable Long id,
                                                  @Valid @RequestBody ApplicationStatusRequest request) {
        return applications.updateStatus(CurrentUser.id(auth), id, request);
    }
}
