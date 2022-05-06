package com.gaspar.modwvwbot.controllers.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Body of /announcement request.
 * @see com.gaspar.modwvwbot.controllers.AnnouncementController
 */
@Data
public class AnnouncementRequest {

    /**
     * Message that should be announced.
     */
    private String message;

    /**
     * Announce on these guilds only. This is optional, if not
     * specified it will announce on all guilds.
     */
    @Nullable
    private List<Long> guildIds;

}
