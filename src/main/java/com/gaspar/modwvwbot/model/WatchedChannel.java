package com.gaspar.modwvwbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Generated;
import javax.persistence.*;

/**
 * Table for watched channels. They are stored as guild-channel pairs. It means
 * that the channel in that guild is watched.
 */
@Entity
@Table(name = "watched_channels", uniqueConstraints
        = @UniqueConstraint(columnNames = {"guild_id", "channel_id"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
public class WatchedChannel {

    public WatchedChannel(Long guildId, Long channelId) {
        this.guildId = guildId;
        this.channelId = channelId;
    }

    /**
     * ID of the table row.
     */
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Guild ID of the watched channel.
     */
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    /**
     * Name of the watched channel.
     */
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

}
