package com.example.jobportal.service;

import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.PageResponse;
import com.example.jobportal.dto.UserDto;
import com.example.jobportal.exception.BadRequestException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.model.Role;
import com.example.jobportal.model.User;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.JobSpecifications;
import com.example.jobportal.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final UserRepository users;
    private final JobRepository jobs;
    private final ApplicationRepository applications;

    public AdminService(UserRepository users, JobRepository jobs, ApplicationRepository applications) {
        this.users = users;
        this.jobs = jobs;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("totalUsers", users.count());
        m.put("jobSeekers", users.countByRole(Role.JOB_SEEKER));
        m.put("employers", users.countByRole(Role.EMPLOYER));
        m.put("totalJobs", jobs.count());
        m.put("openJobs", jobs.countByStatus(JobStatus.OPEN));
        m.put("removedJobs", jobs.countByStatus(JobStatus.REMOVED));
        m.put("applications", applications.count());
        return m;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> listUsers(String q, Role role, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (role != null) {
                p.add(cb.equal(root.get("role"), role));
            }
            if (q != null && !q.isBlank()) {
                p.add(cb.or(
                        JobSpecifications.contains(cb, root.<String>get("fullName"), q),
                        JobSpecifications.contains(cb, root.<String>get("email"), q)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt")));
        return PageResponse.of(users.findAll(spec, pageable), DtoMapper::user);
    }

    public UserDto setUserActive(Long adminId, Long userId, boolean active) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN || user.getId().equals(adminId)) {
            throw new BadRequestException("Administrator accounts cannot be deactivated");
        }
        user.setActive(active);
        return DtoMapper.user(users.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobDto> listJobs(String q, JobStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt")));
        return PageResponse.of(jobs.findAll(JobSpecifications.adminSearch(q, status), pageable), DtoMapper::job);
    }

    public JobDto setJobStatus(Long jobId, JobStatus status) {
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setStatus(status);
        return DtoMapper.job(jobs.save(job));
    }
}
