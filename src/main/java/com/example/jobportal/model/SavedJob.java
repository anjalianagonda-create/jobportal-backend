package com.example.jobportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = @UniqueConstraint(
        name = "uk_saved_job_seeker", columnNames = {"job_id", "seeker_id"}))
public class SavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seeker_id", nullable = false)
    private JobSeekerProfile seeker;

    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    public SavedJob() { }

    public SavedJob(Job job, JobSeekerProfile seeker) {
        this.job = job;
        this.seeker = seeker;
    }

    public Long getId() { return id; }
    public Job getJob() { return job; }
    public JobSeekerProfile getSeeker() { return seeker; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
