package com.gaspar.modwvwbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Table for announcement channels in guilds. Very similar to {@link WatchedChannel}.
 */
@Entity
@Table(name = "announcement_channels", uniqueConstraints
        = @UniqueConstraint(columnNames = {"guild_id", "channel_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AnnouncementChannel {

    public AnnouncementChannel(Long guildId, Long channelId) {
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
