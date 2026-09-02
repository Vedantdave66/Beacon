package com.vedant.jobcopilot.application;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApplicationStatusConverter implements AttributeConverter<ApplicationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ApplicationStatus status) {
        return status == null ? null : status.value();
    }

    @Override
    public ApplicationStatus convertToEntityAttribute(String value) {
        return value == null ? null : ApplicationStatus.fromValue(value);
    }
}
