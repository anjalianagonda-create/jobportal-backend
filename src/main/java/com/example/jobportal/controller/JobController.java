package com.example.jobportal.controller;

import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.WorkMode;
import com.example.jobportal.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public job browsing: smart search and filtering by keyword, location, skill, experience, type and mode. */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobs;

    public JobController(JobService jobs) {
        this.jobs = jobs;
    }

    @GetMapping
    public PageResponse<JobDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return jobs.search(keyword, location, skill, experience, jobType, workMode, sort, page, size);
    }

    @GetMapping("/{id}")
    public JobDto get(@PathVariable Long id) {
        return jobs.getPublic(id);
    }
}
