package com.example.jobportal.service;

import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.dto.EmployerProfileDto;
import com.example.jobportal.dto.EmployerProfileRequest;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.EmployerProfileRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class EmployerService {

    private final EmployerProfileRepository employers;
    private final UserRepository users;
    private final JobRepository jobs;
    private final ApplicationRepository applications;

    public EmployerService(EmployerProfileRepository employers, UserRepository users, JobRepository jobs,
                           ApplicationRepository applications) {
        this.employers = employers;
        this.users = users;
        this.jobs = jobs;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public EmployerProfileDto getProfile(Long userId) {
        return DtoMapper.employer(profile(userId));
    }

    public EmployerProfileDto updateProfile(Long userId, EmployerProfileRequest req) {
        EmployerProfile p = profile(userId);
        if (req.contactName() != null && !req.contactName().isBlank()) {
            p.getUser().setFullName(req.contactName().trim());
            users.save(p.getUser());
        }
        p.setCompanyName(req.companyName().trim());
        p.setIndustry(trim(req.industry()));
        p.setCompanySize(trim(req.companySize()));
        p.setWebsite(trim(req.website()));
        p.setLocation(trim(req.location()));
        p.setDescription(trim(req.description()));
        return DtoMapper.employer(employers.save(p));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats(Long userId) {
        Long id = profile(userId).getId();
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("totalJobs", jobs.countByEmployerId(id));
        m.put("openJobs", jobs.countByEmployerIdAndStatus(id, JobStatus.OPEN));
        m.put("applicants", applications.countByJobEmployerId(id));
        m.put("shortlisted", applications.countByJobEmployerIdAndStatus(id, ApplicationStatus.SHORTLISTED));
        m.put("interviews", applications.countByJobEmployerIdAndStatus(id, ApplicationStatus.INTERVIEW));
        m.put("offers", applications.countByJobEmployerIdAndStatus(id, ApplicationStatus.OFFERED));
        return m;
    }

    private EmployerProfile profile(Long userId) {
        return employers.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
    }

    private static String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
