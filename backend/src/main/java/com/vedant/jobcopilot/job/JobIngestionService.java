package com.vedant.jobcopilot.job;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobIngestionService {

    private static final Logger log = LoggerFactory.getLogger(JobIngestionService.class);

    private final List<JobFeedClient> clients;
    private final JobRepository jobRepository;

    public JobIngestionService(List<JobFeedClient> clients, JobRepository jobRepository) {
        this.clients = clients;
        this.jobRepository = jobRepository;
    }

    @Scheduled(cron = "${job-feed.schedule}", zone = "${job-feed.time-zone}")
    public void scheduledRefresh() {
        RefreshResult result = refreshJobs();
        log.info("Daily job refresh finished: {} received, {} added, {} updated",
                result.received(), result.added(), result.updated());
    }

    @Transactional
    public RefreshResult refreshJobs() {
        Map<String, JobFeedItem> uniqueItems = new LinkedHashMap<>();
        for (JobFeedClient client : clients) {
            for (JobFeedItem item : client.fetchJobs()) {
                if (isValid(item)) {
                    uniqueItems.put(item.source().value() + ":" + item.externalId(), item);
                }
            }
        }

        int added = 0;
        int updated = 0;
        for (JobFeedItem item : uniqueItems.values()) {
            Job job = jobRepository.findBySourceAndExternalId(item.source(), item.externalId()).orElse(null);
            if (job == null) {
                job = new Job(item);
                added++;
            } else {
                job.updateFrom(item);
                updated++;
            }
            jobRepository.save(job);
        }
        return new RefreshResult(uniqueItems.size(), added, updated);
    }

    private boolean isValid(JobFeedItem item) {
        return item.externalId() != null && !item.externalId().isBlank()
                && item.title() != null && !item.title().isBlank()
                && item.url() != null && !item.url().isBlank();
    }

    public record RefreshResult(int received, int added, int updated) {
    }
}
