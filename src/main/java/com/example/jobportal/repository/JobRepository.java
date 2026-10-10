package com.example.jobportal.repository;

import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    long countByStatus(JobStatus status);

    long countByEmployerId(Long employerId);

    long countByEmployerIdAndStatus(Long employerId, JobStatus status);

    /** Returns rows of [jobId, applicantCount] for all jobs of an employer. */
    @Query("select a.job.id, count(a) from Application a where a.job.employer.id = :employerId group by a.job.id")
    List<Object[]> countApplicantsByEmployer(@Param("employerId") Long employerId);
}
