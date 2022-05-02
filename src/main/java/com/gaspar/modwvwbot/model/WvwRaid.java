package com.gaspar.modwvwbot.model;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;

/**
 * Table of wvw raids.
 */
@Entity
@Table(name = "wvw_raids")
@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WvwRaid {

    public static final String DISABLED = "disabled";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Id of the guild which has the event.
     */
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    /**
     * Event start time in the format of Day-xx:xx, like Friday-15:35. Probably storing
     * it as a string is not the best idea.
     */
    @Column(name = "time", nullable = false)
    private String time;

    /**
     * Event duration in the format of XhXXm like 2h, 1h30m, etc. Probably storing
     * it as a string is not the best idea.
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * How much time before the event should there be a reminder. This value can be null,
     * if the reminder is disabled on the event, see {@link #remindTime}.
     */
    @Column(name = "remind_time_minutes")
    @Nullable
    private Integer remindTimeMinutes;

    /**
     * Time when remind should happen for the event. Formatted similarly to {@link #time}. This is
     * exactly {@link #remindTimeMinutes} minutes before {@link #time}. Can be {@link #DISABLED},
     * in that case there is no reminder.
     */
    @Column(name = "remind_time")
    private String remindTime;
}
