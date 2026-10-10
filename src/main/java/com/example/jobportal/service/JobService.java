package com.example.jobportal.service;

import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.JobRequest;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.exception.BadRequestException;
import com.example.jobportal.exception.ForbiddenException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.WorkMode;
import com.example.jobportal.repository.EmployerProfileRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.JobSpecifications;
import com.example.jobportal.util.SkillUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class JobService {

    private final JobRepository jobs;
    private final EmployerProfileRepository employers;

    public JobService(JobRepository jobs, EmployerProfileRepository employers) {
        this.jobs = jobs;
        this.employers = employers;
    }

    // ---------------------------------------------------------------- public

    @Transactional(readOnly = true)
    public PageResponse<JobDto> search(String keyword, String location, String skill, Integer experience,
                                       JobType jobType, WorkMode workMode, String sort, int page, int size) {
        Sort order = switch (sort == null ? "newest" : sort) {
            case "salary" -> Sort.by(Sort.Order.desc("salaryMax"), Sort.Order.desc("createdAt"));
            case "deadline" -> Sort.by(Sort.Order.asc("deadline"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), order);
        Page<Job> result = jobs.findAll(
                JobSpecifications.publicSearch(keyword, location, skill, experience, jobType, workMode), pageable);
        return PageResponse.of(result, DtoMapper::job);
    }

    @Transactional(readOnly = true)
    public JobDto getPublic(Long id) {
        Job job = find(id);
        if (job.getStatus() == JobStatus.REMOVED || !job.getEmployer().getUser().isActive()) {
            throw new ResourceNotFoundException("Job not found");
        }
        return DtoMapper.job(job);
    }

    // -------------------------------------------------------------- employer

    @Transactional(readOnly = true)
    public List<JobDto> listForEmployer(Long employerUserId) {
        EmployerProfile employer = employerOf(employerUserId);
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : jobs.countApplicantsByEmployer(employer.getId())) {
            counts.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return jobs.findByEmployerIdOrderByCreatedAtDesc(employer.getId()).stream()
                .filter(j -> j.getStatus() != JobStatus.REMOVED)
                .map(j -> DtoMapper.job(j, counts.getOrDefault(j.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDto getOwned(Long employerUserId, Long jobId) {
        return DtoMapper.job(owned(employerUserId, jobId));
    }

    public JobDto create(Long employerUserId, JobRequest req) {
        validate(req, true);
        Job job = new Job();
        job.setEmployer(employerOf(employerUserId));
        apply(job, req);
        return DtoMapper.job(jobs.save(job), 0L);
    }

    public JobDto update(Long employerUserId, Long jobId, JobRequest req) {
        Job job = owned(employerUserId, jobId);
        if (job.getStatus() == JobStatus.REMOVED) {
            throw new ForbiddenException("This job was removed by an administrator and cannot be edited");
        }
        validate(req, false);
        apply(job, req);
        return DtoMapper.job(jobs.save(job));
    }

    public JobDto changeStatus(Long employerUserId, Long jobId, JobStatus status) {
        Job job = owned(employerUserId, jobId);
        if (status == JobStatus.REMOVED) {
            throw new BadRequestException("Employers can only open or close a job");
        }
        if (job.getStatus() == JobStatus.REMOVED) {
            throw new ForbiddenException("This job was removed by an administrator");
        }
        job.setStatus(status);
        return DtoMapper.job(jobs.save(job));
    }

    // --------------------------------------------------------------- helpers

    private Job find(Long id) {
        return jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private EmployerProfile employerOf(Long userId) {
        return employers.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
    }

    private Job owned(Long employerUserId, Long jobId) {
        Job job = find(jobId);
        if (!job.getEmployer().getUser().getId().equals(employerUserId)) {
            throw new ForbiddenException("You can only manage your own job postings");
        }
        return job;
    }

    private void validate(JobRequest req, boolean creating) {
        if (req.maxExperience() != null && req.maxExperience() < req.minExperience()) {
            throw new BadRequestException("Maximum experience cannot be lower than minimum experience");
        }
        if (req.salaryMin() != null && req.salaryMax() != null && req.salaryMax() < req.salaryMin()) {
            throw new BadRequestException("Maximum salary cannot be lower than minimum salary");
        }
        if (creating && req.deadline() != null && req.deadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("Application deadline cannot be in the past");
        }
    }

    private void apply(Job job, JobRequest req) {
        job.setTitle(req.title().trim());
        job.setDescription(req.description().trim());
        job.setEligibility(blankToNull(req.eligibility()));
        job.setSkills(SkillUtil.join(req.skills()));
        job.setLocation(req.location().trim());
        job.setJobType(req.jobType());
        job.setWorkMode(req.workMode());
        job.setMinExperience(req.minExperience());
        job.setMaxExperience(req.maxExperience());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        job.setOpenings(req.openings() == null ? 1 : req.openings());
        job.setDeadline(req.deadline());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
