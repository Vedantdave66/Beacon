package com.vedant.jobcopilot.application;

public enum ApplicationStatus {
    SAVED("saved"),
    APPLIED("applied"),
    INTERVIEW("interview"),
    OFFER("offer"),
    REJECTED("rejected");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ApplicationStatus fromValue(String value) {
        for (ApplicationStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown application status: " + value);
    }
}
