package com.vedant.jobcopilot.application;

import java.util.List;
import java.util.UUID;

import com.vedant.jobcopilot.job.Job;
import com.vedant.jobcopilot.job.JobRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public List<JobApplication> listApplications() {
        return applicationRepository.findAllWithJobs();
    }

    @Transactional
    public JobApplication saveJob(UUID jobId) {
        return applicationRepository.findByJobId(jobId).orElseGet(() -> {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
            return applicationRepository.save(new JobApplication(job));
        });
    }

    @Transactional
    public JobApplication updateStatus(UUID applicationId, String statusValue) {
        ApplicationStatus status;
        try {
            status = ApplicationStatus.fromValue(statusValue);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        application.moveTo(status);
        return application;
    }
}
