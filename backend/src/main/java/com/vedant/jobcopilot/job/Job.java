package com.vedant.jobcopilot.job;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jobs", uniqueConstraints = @UniqueConstraint(columnNames = { "source", "external_id" }))
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Convert(converter = JobSourceConverter.class)
    @Column(nullable = false)
    private JobSource source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    private String location;

    @Column(name = "salary_range")
    private String salaryRange;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    protected Job() {
    }

    public Job(JobFeedItem item) {
        this.source = item.source();
        this.externalId = item.externalId();
        updateFrom(item);
    }

    public void updateFrom(JobFeedItem item) {
        title = item.title();
        company = item.company();
        description = item.description();
        url = item.url();
        location = item.location();
        salaryRange = item.salaryRange();
        postedAt = item.postedAt();
        fetchedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public JobSource getSource() {
        return source;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getLocation() {
        return location;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}
