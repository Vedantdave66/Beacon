package com.vedant.jobcopilot.profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "parsed_resume")
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "raw_text", nullable = false)
    private String rawText;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] skills = new String[0];

    @Column(name = "years_experience")
    private BigDecimal yearsExperience;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tech_stack", columnDefinition = "text[]", nullable = false)
    private String[] techStack = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "past_titles", columnDefinition = "text[]", nullable = false)
    private String[] pastTitles = new String[0];

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParsedResume() {
    }

    public ParsedResume(Profile profile) {
        this.profile = profile;
    }

    public void update(
            String rawText,
            String[] skills,
            BigDecimal yearsExperience,
            String[] techStack,
            String[] pastTitles) {
        this.rawText = rawText;
        this.skills = skills == null ? new String[0] : skills;
        this.yearsExperience = yearsExperience;
        this.techStack = techStack == null ? new String[0] : techStack;
        this.pastTitles = pastTitles == null ? new String[0] : pastTitles;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getRawText() {
        return rawText;
    }

    public String[] getSkills() {
        return skills;
    }

    public BigDecimal getYearsExperience() {
        return yearsExperience;
    }

    public String[] getTechStack() {
        return techStack;
    }

    public String[] getPastTitles() {
        return pastTitles;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
