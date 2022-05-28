package com.gaspar.modwvwbot.controllers;

import com.gaspar.modwvwbot.controllers.dto.GuildsResponse;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.services.AuthorizationService;
import com.gaspar.modwvwbot.services.botapi.DiscordGuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guilds")
@Slf4j
@RequiredArgsConstructor
public class GuildsController {

    private final AuthorizationService authorizationService;
    private final DiscordGuildService discordGuildService;

    /**
     * Get all guilds the bot is participating in.
     * @param token Security token.
     * @see GuildsResponse
     */
    @GetMapping
    public GuildsResponse getGuilds(@RequestParam String token) {
        log.info("'GET guilds' request was received.");
        if(authorizationService.isUnauthorizedToCallApi(token)) {
            throw new UnauthorizedException("Invalid security token!");
        }
        return discordGuildService.getAllGuilds();
    }

}
