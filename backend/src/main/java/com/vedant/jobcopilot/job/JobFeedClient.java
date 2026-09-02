package com.vedant.jobcopilot.job;

import java.util.List;

public interface JobFeedClient {
    List<JobFeedItem> fetchJobs();
}
