package com.vedant.jobcopilot.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobRepository jobRepository;
    private final JobIngestionService ingestionService;

    public JobController(JobRepository jobRepository, JobIngestionService ingestionService) {
        this.jobRepository = jobRepository;
        this.ingestionService = ingestionService;
    }

    @GetMapping
    public List<JobView> listJobs() {
        return jobRepository.findAllByOrderByPostedAtDesc().stream().map(JobView::from).toList();
    }

    @PostMapping("/refresh")
    public JobIngestionService.RefreshResult refreshJobs() {
        return ingestionService.refreshJobs();
    }

    public record JobView(
            UUID id,
            String source,
            String title,
            String company,
            String description,
            String url,
            String location,
            String salaryRange,
            Instant postedAt) {

        public static JobView from(Job job) {
            return new JobView(
                    job.getId(),
                    job.getSource().value(),
                    job.getTitle(),
                    job.getCompany(),
                    job.getDescription(),
                    job.getUrl(),
                    job.getLocation(),
                    job.getSalaryRange(),
                    job.getPostedAt());
        }
    }
}
