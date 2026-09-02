package com.vedant.jobcopilot.job;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JobSourceConverter implements AttributeConverter<JobSource, String> {

    @Override
    public String convertToDatabaseColumn(JobSource source) {
        return source == null ? null : source.value();
    }

    @Override
    public JobSource convertToEntityAttribute(String value) {
        return value == null ? null : JobSource.fromValue(value);
    }
}
