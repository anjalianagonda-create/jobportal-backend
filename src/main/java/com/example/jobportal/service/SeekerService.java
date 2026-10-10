package com.example.jobportal.service;

import com.example.jobportal.dto.DtoMapper;
import com.example.jobportal.dto.JobDto;
import com.example.jobportal.dto.SeekerProfileDto;
import com.example.jobportal.dto.SeekerProfileRequest;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobSeekerProfile;
import com.example.jobportal.model.SavedJob;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.JobSeekerProfileRepository;
import com.example.jobportal.repository.SavedJobRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.util.SkillUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SeekerService {

    private final JobSeekerProfileRepository seekers;
    private final UserRepository users;
    private final JobRepository jobs;
    private final SavedJobRepository saved;
    private final ApplicationRepository applications;

    public SeekerService(JobSeekerProfileRepository seekers, UserRepository users, JobRepository jobs,
                         SavedJobRepository saved, ApplicationRepository applications) {
        this.seekers = seekers;
        this.users = users;
        this.jobs = jobs;
        this.saved = saved;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public SeekerProfileDto getProfile(Long userId) {
        return DtoMapper.seeker(profile(userId));
    }

    public SeekerProfileDto updateProfile(Long userId, SeekerProfileRequest req) {
        JobSeekerProfile p = profile(userId);
        if (req.fullName() != null && !req.fullName().isBlank()) {
            p.getUser().setFullName(req.fullName().trim());
            users.save(p.getUser());
        }
        p.setPhone(trim(req.phone()));
        p.setLocation(trim(req.location()));
        p.setHeadline(trim(req.headline()));
        p.setSummary(trim(req.summary()));
        p.setSkills(SkillUtil.join(req.skills()));
        p.setEducation(trim(req.education()));
        p.setWorkExperience(trim(req.workExperience()));
        p.setExperienceYears(req.experienceYears() == null ? 0 : req.experienceYears());
        p.setResumeUrl(trim(req.resumeUrl()));
        p.setLinkedinUrl(trim(req.linkedinUrl()));
        return DtoMapper.seeker(seekers.save(p));
    }

    @Transactional(readOnly = true)
    public List<JobDto> listSaved(Long userId) {
        return saved.findBySeekerIdOrderBySavedAtDesc(profile(userId).getId()).stream()
                .map(s -> DtoMapper.job(s.getJob())).toList();
    }

    public void saveJob(Long userId, Long jobId) {
        JobSeekerProfile p = profile(userId);
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!saved.existsBySeekerIdAndJobId(p.getId(), jobId)) {
            saved.save(new SavedJob(job, p));
        }
    }

    public void unsaveJob(Long userId, Long jobId) {
        JobSeekerProfile p = profile(userId);
        saved.findBySeekerIdAndJobId(p.getId(), jobId).ifPresent(saved::delete);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats(Long userId) {
        Long id = profile(userId).getId();
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("applications", applications.countBySeekerId(id));
        m.put("shortlisted", applications.countBySeekerIdAndStatus(id, ApplicationStatus.SHORTLISTED));
        m.put("interviews", applications.countBySeekerIdAndStatus(id, ApplicationStatus.INTERVIEW));
        m.put("offers", applications.countBySeekerIdAndStatus(id, ApplicationStatus.OFFERED));
        m.put("savedJobs", saved.countBySeekerId(id));
        return m;
    }

    private JobSeekerProfile profile(Long userId) {
        return seekers.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found"));
    }

    private static String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
