package com.gaspar.modwvwbot.controllers;

import com.gaspar.modwvwbot.controllers.dto.AnnouncementRequest;
import com.gaspar.modwvwbot.controllers.dto.AnnouncementResponse;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.services.ChannelCommandsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * This controller allows creating announcements on guild
 * announcement channels.
 */
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    @Value("${com.gaspar.modwvwbot.security_token}")
    private String securityToken;

    private final ChannelCommandsService channelCommandsService;

    /**
     * Broadcast an announcement across guilds.
     * @param request Request with details.
     * @param token Security token which is required for the operation.
     * @return Response with details.
     * @see AnnouncementRequest
     * @see AnnouncementResponse
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse postAnnouncementInGuilds(
            @RequestBody AnnouncementRequest request,
            @RequestParam(name = "token") String token) {
        if(!securityToken.equals(token)) {
            throw new UnauthorizedException("Invalid security token!");
        }
        return channelCommandsService.publishAnnouncements(request);
    }

}
