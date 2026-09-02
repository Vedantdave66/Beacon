package com.vedant.jobcopilot.job;

import java.time.Instant;

public record JobFeedItem(
        JobSource source,
        String externalId,
        String title,
        String company,
        String description,
        String url,
        String location,
        String salaryRange,
        Instant postedAt) {
}
