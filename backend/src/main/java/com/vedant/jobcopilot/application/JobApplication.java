package com.vedant.jobcopilot.application;

import java.time.Instant;
import java.util.UUID;

import com.vedant.jobcopilot.job.Job;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Convert(converter = ApplicationStatusConverter.class)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.SAVED;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "cover_letter_text", columnDefinition = "text")
    private String coverLetterText;

    @Column(name = "resume_version")
    private String resumeVersion;

    protected JobApplication() {
    }

    public JobApplication(Job job) {
        this.job = job;
    }

    public void moveTo(ApplicationStatus status) {
        this.status = status;
        if (status == ApplicationStatus.APPLIED && appliedAt == null) {
            appliedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Job getJob() {
        return job;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getCoverLetterText() {
        return coverLetterText;
    }

    public String getResumeVersion() {
        return resumeVersion;
    }
}
