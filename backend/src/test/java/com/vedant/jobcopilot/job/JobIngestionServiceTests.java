package com.vedant.jobcopilot.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class JobIngestionServiceTests {

    @Test
    void deduplicatesFeedItemsBeforeSaving() {
        JobRepository repository = mock(JobRepository.class);
        JobFeedItem item = new JobFeedItem(
                JobSource.REMOTE_OK,
                "123",
                "Java Developer",
                "Beacon Labs",
                "Build useful things",
                "https://example.com/jobs/123",
                "Remote",
                null,
                Instant.parse("2026-08-30T12:00:00Z"));
        JobFeedClient firstClient = () -> List.of(item);
        JobFeedClient secondClient = () -> List.of(item);
        when(repository.findBySourceAndExternalId(JobSource.REMOTE_OK, "123")).thenReturn(Optional.empty());

        JobIngestionService service = new JobIngestionService(List.of(firstClient, secondClient), repository);

        JobIngestionService.RefreshResult result = service.refreshJobs();

        assertThat(result.received()).isEqualTo(1);
        assertThat(result.added()).isEqualTo(1);
        verify(repository).save(org.mockito.ArgumentMatchers.any(Job.class));
    }
}
