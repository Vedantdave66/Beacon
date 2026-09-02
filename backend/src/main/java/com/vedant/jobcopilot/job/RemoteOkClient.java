package com.vedant.jobcopilot.job;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RemoteOkClient implements JobFeedClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteOkClient.class);

    private final WebClient webClient;

    public RemoteOkClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://remoteok.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "Beacon/1.0")
                .build();
    }

    @Override
    public List<JobFeedItem> fetchJobs() {
        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri("/api")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block(Duration.ofSeconds(20));

            if (response == null) {
                return List.of();
            }
            return response.stream()
                    .filter(item -> FeedValueReader.text(item, "id") != null)
                    .map(this::toFeedItem)
                    .toList();
        } catch (Exception exception) {
            log.warn("RemoteOK refresh failed: {}", exception.getMessage());
            return List.of();
        }
    }

    private JobFeedItem toFeedItem(Map<String, Object> item) {
        return new JobFeedItem(
                JobSource.REMOTE_OK,
                FeedValueReader.text(item, "id"),
                FeedValueReader.text(item, "position"),
                valueOrFallback(FeedValueReader.text(item, "company"), "Unknown company"),
                FeedValueReader.plainText(FeedValueReader.text(item, "description")),
                FeedValueReader.text(item, "url"),
                valueOrFallback(FeedValueReader.text(item, "location"), "Remote"),
                remoteOkSalary(item),
                postedAt(item));
    }

    private String remoteOkSalary(Map<String, Object> item) {
        String minimum = FeedValueReader.text(item, "salary_min");
        String maximum = FeedValueReader.text(item, "salary_max");
        if ((minimum == null || minimum.equals("0")) && (maximum == null || maximum.equals("0"))) {
            return null;
        }
        return "$" + minimum + " – $" + maximum;
    }

    private Instant postedAt(Map<String, Object> item) {
        Object epoch = item.get("epoch");
        if (epoch instanceof Number number) {
            return Instant.ofEpochSecond(number.longValue());
        }
        return FeedValueReader.instant(FeedValueReader.text(item, "date"));
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
