package com.vedant.jobcopilot.job;

public enum JobSource {
    ADZUNA("adzuna"),
    REMOTE_OK("remoteok");

    private final String value;

    JobSource(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static JobSource fromValue(String value) {
        for (JobSource source : values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown job source: " + value);
    }
}
