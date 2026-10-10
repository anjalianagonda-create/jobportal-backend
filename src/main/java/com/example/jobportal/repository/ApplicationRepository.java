package com.example.jobportal.repository;

import com.example.jobportal.model.Application;
import com.example.jobportal.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findBySeekerIdOrderByAppliedAtDesc(Long seekerId);

    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);

    boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId);

    long countBySeekerId(Long seekerId);

    long countBySeekerIdAndStatus(Long seekerId, ApplicationStatus status);

    long countByJobEmployerId(Long employerId);

    long countByJobEmployerIdAndStatus(Long employerId, ApplicationStatus status);
}
