package com.example.jobportal.repository;

import com.example.jobportal.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findBySeekerIdOrderBySavedAtDesc(Long seekerId);

    Optional<SavedJob> findBySeekerIdAndJobId(Long seekerId, Long jobId);

    boolean existsBySeekerIdAndJobId(Long seekerId, Long jobId);

    long countBySeekerId(Long seekerId);
}
