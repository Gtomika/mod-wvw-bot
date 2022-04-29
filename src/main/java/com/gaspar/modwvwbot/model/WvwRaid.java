package com.gaspar.modwvwbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
public class WvwRaid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Id of the guild which has the event.
     */
    @Column(name = "guild_id")
    private Long guildId;

    /**
     * Event start time in the format of Day-xx:xx, like Friday-15:35.
     */
    @Column(name = "time")
    private String time;

    /**
     * Event duration in the format of XhXXm like 2h, 1h30m, etc.
     */
    @Column(name = "duration")
    private String duration;

    /**
     * How much time before the event should there be a reminder. Same format as
     * {@link #duration}, can also be 'disable'.
     */
    @Column(name = "remind_time")
    private String remindTime;
}
