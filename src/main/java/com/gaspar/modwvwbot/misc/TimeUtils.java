package com.gaspar.modwvwbot.misc;

import com.gaspar.modwvwbot.model.WvwRaid;
import org.springframework.lang.Nullable;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for managing time string.
 */
public class TimeUtils {

    private static final Pattern HOUR_MIN_PATTERN = Pattern.compile("^\\d\\d:\\d[05]$");

    private static final Pattern DURATION_PATTERN = Pattern.compile("([0-9]+)([hm])");

    public static final int MINUTES_IN_DAY = 60 * 24;

    /**
     * Check if a string is in the expected time format, e.g Friday-20:00. Must also
     * be rounded to 5 minutes: xx:x5 or xx:x0.
     */
    public static boolean isValidTimeString(String time) {
        var parts = time.split("-");
        if(parts.length != 2) return false;
        String day = parts[0];
        String hourMin = parts[1];
        //validate day
        try {
            DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException a) {
            return false;
        }
        //validate hour + min
        return HOUR_MIN_PATTERN.matcher(hourMin).matches();
    }

    /**
     * Convert time string into standard format, before a save. Things like friday, FrIdAy are valid
     * but they should be saved as Friday.
     * @param time time string, assumed to be valid, passing {@link #isValidTimeString(String)}.
     * @return The standardized time string.
     */
    public static String standardizeTimeString(String time) {
        var parts = time.split("-");
        String day = parts[0];
        //capitalize only first letter
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < day.length(); i++) {
            if(i == 0) {
                builder.append(Character.toUpperCase(day.charAt(i)));
            } else {
                builder.append(Character.toLowerCase(day.charAt(i)));
            }
        }
        //replace
        return time.replace(day, builder.toString());
    }

    /**
     * Parse a duration into an amount if minutes. Examples are: 1h, 1h45m, 30m, ...
     * @return Duration as minutes.
     */
    public static int parseDurationToMinutes(String duration){
        duration = duration.toLowerCase();
        Matcher matcher = DURATION_PATTERN.matcher(duration);
        Instant instant = Instant.EPOCH;
        while(matcher.find()){
            int num = Integer.parseInt(matcher.group(1));
            String typ = matcher.group(2);
            switch (typ) {
                case "h":
                    instant = instant.plus(Duration.ofHours(num));
                    break;
                case "m":
                    instant = instant.plus(Duration.ofMinutes(num));
                    break;
            }
        }
        return (int)instant.getEpochSecond() / 60;
    }

    /**
     * Convert minutes to duration string.
     * @param minutes Valid minutes, so at least 1.
     * @return String like 1h30m, 15m and so on.
     */
    public static String createDurationStringFromMinutes(long minutes) {
        long days = minutes / (60 * 24);
        long hours = minutes / 60;
        long minutesLeft = minutes - hours * 60;
        StringBuilder builder = new StringBuilder();
        if(days > 0) {
            builder.append(days).append("d");
        }
        if(hours > 0) {
            builder.append(hours).append("h");
        }
        if(minutesLeft > 0) {
            builder.append(minutesLeft).append("m");
        }
        return builder.toString();
    }

    /**
     * Convert minutes to hungarian duration string. Days, hours and minutes are supported.
     */
    public static String createHungarianDurationStringFromMinutes(long minutes) {
        long days = minutes / (60 * 24);
        long hours = (minutes - (days*60*24)) / 60;
        long minutesLeft = minutes - ((hours * 60) + (days * 60 * 24));
        StringBuilder builder = new StringBuilder();
        if(days > 0) {
            builder.append(days).append(" nap, ");
        }
        if(hours > 0) {
            builder.append(hours).append(" óra, ");
        }
        if(minutesLeft > 0) {
            builder.append(minutesLeft).append(" perc");
        }
        if(builder.length() == 0) {
            builder.append("Nagyon kevés");
        }
        return builder.toString();
    }

    /**
     * Convert seconds to hungarian duration. Minutes and seconds are supported.
     */
    public static String createHungarianDurationStringFromSeconds(long secondsTotal) {
        long minutes = secondsTotal / 60;
        long seconds = secondsTotal - minutes*60;
        StringBuilder builder = new StringBuilder();
        if(minutes > 0) {
            builder.append(minutes).append(" perc, ");
        }
        if(seconds > 0) {
            builder.append(seconds).append(" másodperc");
        }
        return builder.toString();
    }

    /**
     * See {@link #getTimeStringRoundedToFiveMinutes(LocalDateTime)}, but with the
     * current time.
     */
    public static String getCurrentTimeStringRoundedFiveMinutes() {
        return getTimeStringRoundedToFiveMinutes(LocalDateTime.now());
    }

    /**
     * Gets the time formatted as used by the bot to store timestamps, e. g Monday-14:35. The
     * time is always rounded to the last exact 5 minutes. For example if it is 20:01 it will still return 20:00.
     * This is useful if the scheduler is delayed for some reason.
     */
    public static String getTimeStringRoundedToFiveMinutes(LocalDateTime localDateTime) {
        String day = localDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.UK);
        String hours = String.format("%2s", localDateTime.getHour()).replaceAll(" ", "0");

        int roundedMinute = localDateTime.getMinute();
        while(roundedMinute % 5 != 0) {
            roundedMinute--;
        }

        String minutes = String.format("%2s", roundedMinute).replaceAll(" ", "0");
        return day + "-" + hours + ":" + minutes;
    }

    /**
     * Convert date to time string, no roundings.
     */
    public static String getTimeString(LocalDateTime localDateTime) {
        String day = localDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.UK);
        String hours = String.format("%2s", localDateTime.getHour()).replaceAll(" ", "0");

        String minutes = String.format("%2s", localDateTime.getMinute()).replaceAll(" ", "0");
        return day + "-" + hours + ":" + minutes;
    }

    /**
     * Creates a time string that is the given amount of minutes before the given
     * time string.
     * @param time Start time.
     * @param minutes Minutes to subtract from start time. Can be null, which means the reminder
     *                is disabled, and {@link com.gaspar.modwvwbot.model.WvwRaid#DISABLED} is returned.
     */
    public static String createReminderTimeStringRoundedToFiveMinutes(String time, @Nullable Integer minutes) {
        if(minutes == null) {
            return WvwRaid.DISABLED;
        }
        //get LocalDateTime of time string
        LocalDateTime date = convertTimeStringToDate(time);
        //subtract minutes
        date = date.minusMinutes(minutes);
        //convert back to string
        return getTimeStringRoundedToFiveMinutes(date);
    }

    private static LocalDateTime convertTimeStringToDate(String time) {
        var parts = time.split("-");
        //get time at the specified day
        DayOfWeek day = DayOfWeek.valueOf(parts[0].toUpperCase());
        LocalDateTime date = LocalDateTime.now().with(TemporalAdjusters.next(day));
        //adjust to hour and minutes
        var hourMin = parts[1].split(":");
        String hourString = hourMin[0];
        if(hourString.startsWith("0")) { //in case of 05 -> make it 5
            hourString = String.valueOf(hourString.charAt(1));
        }
        date = date.withHour(Integer.parseInt(hourString));

        String minString = hourMin[1];
        if(minString.startsWith("0")) {
            minString = String.valueOf(minString.charAt(1));
        }
        return date.withMinute(Integer.parseInt(minString));
    }

    /**
     * Convert valid time string into hungarian version. Friday-10:00 -> Péntek-10:00
     */
    public static String createHungarianTimeString(String time) {
        var parts = time.split("-");
        return time.replace(parts[0], hungarianDay(parts[0]));
    }

    private static String hungarianDay(String day) {
        switch (day) {
            case "Monday":
                return "Hétfő";
            case "Tuesday":
                return "Kedd";
            case "Wednesday":
                return "Szerda";
            case "Thursday":
                return "Csütörtök";
            case "Friday":
                return "Péntek";
            case "Saturday":
                return "Szombat";
            case "Sunday":
                return "Vasárnap";
            default:
                throw new IllegalArgumentException("No such day: " + day);
        }
    }

    public static int getHourOffset() {
        LocalDate lastSundayOfMarch = YearMonth.of(Year.now().getValue(), Month.MARCH)
                .atEndOfMonth()
                .with( TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate lastSundayOfOctober = YearMonth.of(Year.now().getValue(), Month.OCTOBER)
                .atEndOfMonth()
                .with( TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate now = LocalDate.now();
        if(now.isAfter(lastSundayOfMarch) && now.isBefore(lastSundayOfOctober)) {
            return 2; //summer time
        } else {
            return 1; //not summer time
        }
    }
}
