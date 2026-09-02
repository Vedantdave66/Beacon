package com.vedant.jobcopilot.job;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AdzunaClient implements JobFeedClient {

    private static final Logger log = LoggerFactory.getLogger(AdzunaClient.class);

    private final WebClient webClient;
    private final String appId;
    private final String appKey;
    private final String country;
    private final String query;

    public AdzunaClient(
            WebClient.Builder webClientBuilder,
            @Value("${job-feed.adzuna.app-id}") String appId,
            @Value("${job-feed.adzuna.app-key}") String appKey,
            @Value("${job-feed.adzuna.country}") String country,
            @Value("${job-feed.adzuna.query}") String query) {
        this.webClient = webClientBuilder.baseUrl("https://api.adzuna.com").build();
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
        this.query = query;
    }

    @Override
    public List<JobFeedItem> fetchJobs() {
        if (appId.isBlank() || appKey.isBlank()) {
            log.info("Adzuna credentials are not configured; skipping Adzuna refresh");
            return List.of();
        }

        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/api/jobs/{country}/search/1")
                            .queryParam("app_id", appId)
                            .queryParam("app_key", appKey)
                            .queryParam("results_per_page", 50)
                            .queryParam("what", query)
                            .build(country))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block(Duration.ofSeconds(20));

            return resultMaps(response).stream().map(this::toFeedItem).toList();
        } catch (Exception exception) {
            log.warn("Adzuna refresh failed: {}", exception.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resultMaps(Map<String, Object> response) {
        if (response == null || !(response.get("results") instanceof List<?> results)) {
            return Collections.emptyList();
        }
        return (List<Map<String, Object>>) (List<?>) results;
    }

    private JobFeedItem toFeedItem(Map<String, Object> item) {
        BigDecimal minimum = FeedValueReader.decimal(item, "salary_min");
        BigDecimal maximum = FeedValueReader.decimal(item, "salary_max");
        return new JobFeedItem(
                JobSource.ADZUNA,
                FeedValueReader.text(item, "id"),
                FeedValueReader.text(item, "title"),
                valueOrFallback(FeedValueReader.nestedText(item, "company", "display_name"), "Unknown company"),
                FeedValueReader.plainText(FeedValueReader.text(item, "description")),
                FeedValueReader.text(item, "redirect_url"),
                FeedValueReader.nestedText(item, "location", "display_name"),
                salaryRange(minimum, maximum),
                FeedValueReader.instant(FeedValueReader.text(item, "created")));
    }

    private String salaryRange(BigDecimal minimum, BigDecimal maximum) {
        if (minimum == null && maximum == null) {
            return null;
        }
        if (minimum == null) {
            return "Up to $" + maximum.toPlainString();
        }
        if (maximum == null) {
            return "From $" + minimum.toPlainString();
        }
        return "$" + minimum.toPlainString() + " – $" + maximum.toPlainString();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
