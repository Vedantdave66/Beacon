package com.vedant.jobcopilot.profile;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParsedResumeRepository extends JpaRepository<ParsedResume, UUID> {

    Optional<ParsedResume> findByProfileId(UUID profileId);

    boolean existsByProfileId(UUID profileId);
}
