package com.example.jobportal.service;

import com.example.jobportal.dto.ApplicationDto;
import com.example.jobportal.dto.ApplicationStatusRequest;
import com.example.jobportal.dto.ApplyRequest;
import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.exception.BadRequestException;
import com.example.jobportal.exception.ConflictException;
import com.example.jobportal.exception.ForbiddenException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.Application;
import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobSeekerProfile;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.JobSeekerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ApplicationService {

    /** Statuses an employer may move an application to. */
    private static final Set<ApplicationStatus> EMPLOYER_STATUSES = EnumSet.of(
            ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW,
            ApplicationStatus.OFFERED, ApplicationStatus.REJECTED);

    private final ApplicationRepository applications;
    private final JobRepository jobs;
    private final JobSeekerProfileRepository seekers;

    public ApplicationService(ApplicationRepository applications, JobRepository jobs,
                              JobSeekerProfileRepository seekers) {
        this.applications = applications;
        this.jobs = jobs;
        this.seekers = seekers;
    }

    // ------------------------------------------------------------ job seeker

    public ApplicationDto apply(Long seekerUserId, Long jobId, ApplyRequest req) {
        JobSeekerProfile seeker = seekerOf(seekerUserId);
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        boolean expired = job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now());
        if (job.getStatus() != JobStatus.OPEN || expired || !job.getEmployer().getUser().isActive()) {
            throw new BadRequestException("This job is no longer accepting applications");
        }
        if (applications.existsByJobIdAndSeekerId(jobId, seeker.getId())) {
            throw new ConflictException("You have already applied for this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setSeeker(seeker);
        application.setCoverLetter(req == null || req.coverLetter() == null || req.coverLetter().isBlank()
                ? null : req.coverLetter().trim());
        return DtoMapper.application(applications.save(application), false);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> listForSeeker(Long seekerUserId) {
        JobSeekerProfile seeker = seekerOf(seekerUserId);
        return applications.findBySeekerIdOrderByAppliedAtDesc(seeker.getId()).stream()
                .map(a -> DtoMapper.application(a, false)).toList();
    }

    public ApplicationDto withdraw(Long seekerUserId, Long applicationId) {
        Application application = find(applicationId);
        if (!application.getSeeker().getUser().getId().equals(seekerUserId)) {
            throw new ForbiddenException("You can only withdraw your own applications");
        }
        ApplicationStatus current = application.getStatus();
        if (current == ApplicationStatus.WITHDRAWN || current == ApplicationStatus.REJECTED
                || current == ApplicationStatus.OFFERED) {
            throw new BadRequestException("This application can no longer be withdrawn");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        return DtoMapper.application(applications.save(application), false);
    }

    // -------------------------------------------------------------- employer

    @Transactional(readOnly = true)
    public List<ApplicationDto> listForJob(Long employerUserId, Long jobId) {
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getEmployer().getUser().getId().equals(employerUserId)) {
            throw new ForbiddenException("You can only view applicants of your own jobs");
        }
        return applications.findByJobIdOrderByAppliedAtDesc(jobId).stream()
                .map(a -> DtoMapper.application(a, true)).toList();
    }

    public ApplicationDto updateStatus(Long employerUserId, Long applicationId, ApplicationStatusRequest req) {
        Application application = find(applicationId);
        if (!application.getJob().getEmployer().getUser().getId().equals(employerUserId)) {
            throw new ForbiddenException("You can only manage applicants of your own jobs");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("The candidate has withdrawn this application");
        }
        if (!EMPLOYER_STATUSES.contains(req.status())) {
            throw new BadRequestException("Invalid status for this action");
        }
        application.setStatus(req.status());
        if (req.note() != null) {
            application.setEmployerNote(req.note().isBlank() ? null : req.note().trim());
        }
        return DtoMapper.application(applications.save(application), true);
    }

    // --------------------------------------------------------------- helpers

    private Application find(Long id) {
        return applications.findById(id).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private JobSeekerProfile seekerOf(Long userId) {
        return seekers.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found"));
    }
}
