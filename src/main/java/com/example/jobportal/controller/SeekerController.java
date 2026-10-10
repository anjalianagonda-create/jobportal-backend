package com.example.jobportal.controller;

import com.example.jobportal.dto.ApplicationDto;
import com.example.jobportal.dto.ApplyRequest;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.SeekerProfileDto;
import com.example.jobportal.dto.SeekerProfileRequest;
import com.example.jobportal.security.CurrentUser;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.SeekerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/seeker")
public class SeekerController {

    private final SeekerService seekers;
    private final ApplicationService applications;

    public SeekerController(SeekerService seekers, ApplicationService applications) {
        this.seekers = seekers;
        this.applications = applications;
    }

    @GetMapping("/stats")
    public Map<String, Long> stats(Authentication auth) {
        return seekers.stats(CurrentUser.id(auth));
    }

    @GetMapping("/profile")
    public SeekerProfileDto profile(Authentication auth) {
        return seekers.getProfile(CurrentUser.id(auth));
    }

    @PutMapping("/profile")
    public SeekerProfileDto updateProfile(Authentication auth, @Valid @RequestBody SeekerProfileRequest request) {
        return seekers.updateProfile(CurrentUser.id(auth), request);
    }

    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDto apply(Authentication auth, @PathVariable Long jobId,
                                @Valid @RequestBody(required = false) ApplyRequest request) {
        return applications.apply(CurrentUser.id(auth), jobId, request);
    }

    @GetMapping("/applications")
    public List<ApplicationDto> myApplications(Authentication auth) {
        return applications.listForSeeker(CurrentUser.id(auth));
    }

    @PatchMapping("/applications/{id}/withdraw")
    public ApplicationDto withdraw(Authentication auth, @PathVariable Long id) {
        return applications.withdraw(CurrentUser.id(auth), id);
    }

    @GetMapping("/saved")
    public List<JobDto> saved(Authentication auth) {
        return seekers.listSaved(CurrentUser.id(auth));
    }

    @PostMapping("/saved/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(Authentication auth, @PathVariable Long jobId) {
        seekers.saveJob(CurrentUser.id(auth), jobId);
    }

    @DeleteMapping("/saved/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsave(Authentication auth, @PathVariable Long jobId) {
        seekers.unsaveJob(CurrentUser.id(auth), jobId);
    }
}
