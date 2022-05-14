package com.gaspar.modwvwbot.misc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    private static Stream<Arguments> provideTimeStrings() {
        return Stream.of(
                Arguments.of("Friday-20:00", true),
                Arguments.of("monday-03:05", true),
                Arguments.of("TuEsDaY-10:15", true),
                Arguments.of("Something-10:15", false),
                Arguments.of("Friday.20:00", false),
                Arguments.of("Sunday-a20:00", false),
                Arguments.of("Sunday-20:02", false) //not ending in 0 or 5
        );
    }

    private static Stream<Arguments> provideValidTimeStrings() {
        return Stream.of(
                Arguments.of("Friday-20:00", "Friday-20:00"),
                Arguments.of("monday-03:05", "Monday-03:05"),
                Arguments.of("TuEsDaY-10:15", "Tuesday-10:15")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTimeStrings")
    public void testIsValidTimeString(String time, boolean valid) {
        assertEquals(valid, TimeUtils.isValidTimeString(time));
    }

    @ParameterizedTest
    @MethodSource("provideValidTimeStrings")
    public void testStandardizeTimeString(String time, String standardized) {
        assertEquals(standardized, TimeUtils.standardizeTimeString(time));
    }

    private static Stream<Arguments> provideDurations() {
        return Stream.of(
                Arguments.of("1h", 60),
                Arguments.of("2h", 120),
                Arguments.of("2h30m", 150),
                Arguments.of("15m", 15),
                Arguments.of("0m", 0),
                Arguments.of("24h", TimeUtils.MINUTES_IN_DAY),
                Arguments.of("invalid", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDurations")
    public void testParseDurationToMinutes(String duration, int minutes) {
        assertEquals(minutes, TimeUtils.parseDurationToMinutes(duration));
    }

    private static Stream<Arguments> provideMinutes() {
        return Stream.of(
                Arguments.of("1h", 60),
                Arguments.of("2h", 120),
                Arguments.of("2h30m", 150),
                Arguments.of("15m", 15),
                Arguments.of("24h", TimeUtils.MINUTES_IN_DAY)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMinutes")
    public void testConvertMinutesToString(String duration, int minutes) {
        assertEquals(duration, TimeUtils.createDurationStringFromMinutes(minutes));
    }

}