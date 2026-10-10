package com.example.jobportal.repository;

import com.example.jobportal.model.EmployerProfile;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobStatus;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.WorkMode;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Dynamic query building for the smart search and filtering module. */
public final class JobSpecifications {

    private JobSpecifications() { }

    /** Jobs visible to the public: open, not past deadline, posted by an active employer. */
    public static Specification<Job> publicSearch(String keyword, String location, String skill,
                                                  Integer experience, JobType jobType, WorkMode workMode) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            Join<Job, EmployerProfile> employer = root.join("employer");

            p.add(cb.equal(root.get("status"), JobStatus.OPEN));
            p.add(cb.or(cb.isNull(root.get("deadline")),
                    cb.greaterThanOrEqualTo(root.<LocalDate>get("deadline"), LocalDate.now())));
            p.add(cb.isTrue(employer.get("user").<Boolean>get("active")));

            if (hasText(keyword)) {
                p.add(cb.or(
                        contains(cb, root.<String>get("title"), keyword),
                        contains(cb, root.<String>get("description"), keyword),
                        contains(cb, root.<String>get("skills"), keyword),
                        contains(cb, employer.<String>get("companyName"), keyword)));
            }
            if (hasText(location)) {
                p.add(contains(cb, root.<String>get("location"), location));
            }
            if (hasText(skill)) {
                p.add(contains(cb, root.<String>get("skills"), skill));
            }
            if (experience != null) {
                // Jobs whose minimum requirement the candidate already satisfies
                p.add(cb.lessThanOrEqualTo(root.<Integer>get("minExperience"), experience));
            }
            if (jobType != null) {
                p.add(cb.equal(root.get("jobType"), jobType));
            }
            if (workMode != null) {
                p.add(cb.equal(root.get("workMode"), workMode));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    /** Moderation view for administrators: any status, optional text and status filters. */
    public static Specification<Job> adminSearch(String q, JobStatus status) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            Join<Job, EmployerProfile> employer = root.join("employer");
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (hasText(q)) {
                p.add(cb.or(
                        contains(cb, root.<String>get("title"), q),
                        contains(cb, employer.<String>get("companyName"), q)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static Predicate contains(CriteriaBuilder cb, Expression<String> expr, String term) {
        String escaped = term.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return cb.like(cb.lower(expr), "%" + escaped + "%", '\\');
    }
}
