package com.vedant.jobcopilot.job;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;

final class FeedValueReader {

    private FeedValueReader() {
    }

    static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString().trim();
    }

    @SuppressWarnings("unchecked")
    static String nestedText(Map<String, Object> map, String key, String nestedKey) {
        Object value = map.get(key);
        if (value instanceof Map<?, ?> nested) {
            return text((Map<String, Object>) nested, nestedKey);
        }
        return null;
    }

    static BigDecimal decimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return null;
    }

    static Instant instant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException alsoIgnored) {
                return null;
            }
        }
    }

    static String plainText(String html) {
        if (html == null) {
            return null;
        }
        String withoutTags = html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return HtmlUtils.htmlUnescape(withoutTags);
    }
}
