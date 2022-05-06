package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response when /announcement endpoint is invoked.
 * @see com.gaspar.modwvwbot.controllers.AnnouncementController
 */
@Data
@AllArgsConstructor
public class AnnouncementResponse {

    /**
     * Response message.
     */
    private String message;

    /**
     * On how many channels was the announcement posted.
     */
    private int successfulPostCount;

    /**
     * On how many channels did the post failed.
     */
    private int failedPostCount;

    /**
     * In how many guilds was the announcement posted.
     */
    private int affectedGuildCount;

}
