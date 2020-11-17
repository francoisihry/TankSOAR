package com.tank.soar.worker_orchestrator.domain;

import org.apache.commons.lang3.Validate;

import java.time.*;
import java.util.Objects;

public final class UTCZonedDateTime {

    private final ZonedDateTime zonedDateTime;

    private UTCZonedDateTime(final ZonedDateTime zonedDateTime) {
        this.zonedDateTime = Objects.requireNonNull(zonedDateTime);
        Validate.validState(zonedDateTime.getOffset().equals(ZoneOffset.UTC));
    }

    private UTCZonedDateTime(final LocalDateTime localDateTime) {
        this(localDateTime, ZoneOffset.UTC);
    }

    private UTCZonedDateTime(final LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        this(ZonedDateTime.of(localDateTime, zoneOffset));
    }

    public static UTCZonedDateTime now() {
        return new UTCZonedDateTime(ZonedDateTime.now(ZoneOffset.UTC));
    }

    public static UTCZonedDateTime of(final Long ofEpochSecond) {
        return new UTCZonedDateTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(ofEpochSecond), ZoneOffset.UTC));
    }

    public static UTCZonedDateTime of(final int year, final Month month, final int dayOfMonth,
                                      final int hour, final int minute, final int second) {
        return new UTCZonedDateTime(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second));
    }

    public static UTCZonedDateTime of(final LocalDateTime localDateTime,
                                      final String zoneOffset) {
        return new UTCZonedDateTime(localDateTime, ZoneOffset.of(zoneOffset));
    }

    public static UTCZonedDateTime of(final ZonedDateTime zonedDateTime) {
        return new UTCZonedDateTime(zonedDateTime);
    }

    public ZonedDateTime zonedDateTime() {
        return zonedDateTime;
    }

    public LocalDateTime localDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    public ZoneOffset zoneOffset() {
        return zonedDateTime.getOffset();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UTCZonedDateTime)) return false;
        UTCZonedDateTime that = (UTCZonedDateTime) o;
        return Objects.equals(zonedDateTime, that.zonedDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zonedDateTime);
    }

    @Override
    public String toString() {
        return "UTCZonedDateTime{" +
                "zonedDateTime=" + zonedDateTime +
                '}';
    }
}
