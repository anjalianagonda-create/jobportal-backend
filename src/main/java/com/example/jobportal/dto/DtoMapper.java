package com.example.jobportal.dto;

import com.example.jobportal.model.Application;
import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobSeekerProfile;
import com.example.jobportal.model.User;
import com.example.jobportal.util.SkillUtil;

/** Central place for entity to DTO conversion so controllers never expose entities. */
public final class DtoMapper {

    private DtoMapper() { }

    public static UserDto user(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt());
    }

    public static SeekerProfileDto seeker(JobSeekerProfile p) {
        User u = p.getUser();
        return new SeekerProfileDto(p.getId(), u.getId(), u.getFullName(), u.getEmail(), p.getPhone(),
                p.getLocation(), p.getHeadline(), p.getSummary(), SkillUtil.parse(p.getSkills()),
                p.getEducation(), p.getWorkExperience(), p.getExperienceYears(), p.getResumeUrl(),
                p.getLinkedinUrl());
    }

    public static EmployerProfileDto employer(EmployerProfile p) {
        User u = p.getUser();
        return new EmployerProfileDto(p.getId(), u.getId(), u.getFullName(), u.getEmail(), p.getCompanyName(),
                p.getIndustry(), p.getCompanySize(), p.getWebsite(), p.getLocation(), p.getDescription());
    }

    public static JobDto job(Job j, Long applicantCount) {
        EmployerProfile e = j.getEmployer();
        return new JobDto(j.getId(), j.getTitle(), j.getDescription(), j.getEligibility(),
                SkillUtil.parse(j.getSkills()), j.getLocation(), j.getJobType(), j.getWorkMode(),
                j.getMinExperience(), j.getMaxExperience(), j.getSalaryMin(), j.getSalaryMax(),
                j.getOpenings(), j.getDeadline(), j.getStatus(), j.getCreatedAt(), e.getId(),
                e.getCompanyName(), e.getWebsite(), e.getIndustry(), e.getDescription(), applicantCount);
    }

    public static JobDto job(Job j) {
        return job(j, null);
    }

    public static ApplicationDto application(Application a, boolean includeApplicant) {
        Job j = a.getJob();
        return new ApplicationDto(a.getId(), j.getId(), j.getTitle(), j.getEmployer().getCompanyName(),
                j.getLocation(), j.getStatus(), a.getStatus(), a.getCoverLetter(), a.getEmployerNote(),
                a.getAppliedAt(), a.getUpdatedAt(), includeApplicant ? seeker(a.getSeeker()) : null);
    }
}
