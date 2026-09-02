package com.vedant.jobcopilot.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, UUID> {
    Optional<Job> findBySourceAndExternalId(JobSource source, String externalId);

    List<Job> findAllByOrderByPostedAtDesc();
}
