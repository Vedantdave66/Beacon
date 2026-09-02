package com.vedant.jobcopilot.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<JobApplication, UUID> {
    Optional<JobApplication> findByJobId(UUID jobId);

    @Query("select application from JobApplication application join fetch application.job order by application.id desc")
    List<JobApplication> findAllWithJobs();
}
