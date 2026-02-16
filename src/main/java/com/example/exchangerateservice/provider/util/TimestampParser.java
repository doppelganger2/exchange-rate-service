package com.example.exchangerateservice.provider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * Utility class for parsing various timestamp formats from exchange rate providers.
 */
public class TimestampParser {
    private TimestampParser() {}


    private static final Logger log = LoggerFactory.getLogger(TimestampParser.class);

    /**
     * Parses a timestamp string using multiple strategies.
     * Returns Instant.now() if parsing fails.
     *
     * @param timestamp the timestamp string to parse
     * @return parsed Instant or Instant.now() if parsing fails
     */
    public static Instant parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Instant.now();
        }

        String normalized = timestamp.trim();

        // Try direct ISO-8601 instant parsing
        Instant result = tryParse(normalized, Instant::parse);
        if (result != null) return result;

        // Normalize space to 'T' for ISO formats
        if (normalized.contains(" ") && !normalized.contains("T")) {
            normalized = normalized.replace(" ", "T");
        }

        // Try OffsetDateTime parsing
        result = tryParse(normalized, s -> OffsetDateTime.parse(s).toInstant());
        if (result != null) return result;

        // Try LocalDateTime parsing (assume UTC)
        result = tryParse(normalized, s ->
            LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneOffset.UTC)
                .toInstant()
        );
        if (result != null) return result;

        log.warn("Unable to parse timestamp: {}", timestamp);
        return Instant.now();
    }

    /**
     * Parses a date string (YYYY-MM-DD format) to Instant at the start of the day UTC.
     *
     * @param date the date string to parse
     * @return parsed Instant or Instant.now() if parsing fails
     */
    public static Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return Instant.now();
        }

        try {
            return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException _) {
            log.warn("Unable to parse date: {}", date);
            return Instant.now();
        }
    }

    /**
     * Parses an epoch timestamp (seconds since Unix epoch) to Instant.
     *
     * @param epochSeconds the epoch timestamp in seconds
     * @return parsed Instant or Instant.now() if null
     */
    public static Instant parseEpochSeconds(Long epochSeconds) {
        return epochSeconds != null ? Instant.ofEpochSecond(epochSeconds) : Instant.now();
    }

    private static Instant tryParse(String value, Function<String, Instant> parser) {
        try {
            return parser.apply(value);
        } catch (DateTimeParseException _) {
            return null;
        }
    }
}
