package com.gaspar.modwvwbot.controllers;

import com.gaspar.modwvwbot.controllers.dto.GuildStatisticResponse;
import com.gaspar.modwvwbot.controllers.dto.MonthlyCommandUsageResponse;
import com.gaspar.modwvwbot.controllers.dto.UsageResponse;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.services.AuthorizationService;
import com.gaspar.modwvwbot.services.botapi.CommandUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {

    private final AuthorizationService authorizationService;
    private final CommandUsageService commandUsageService;

    @GetMapping
    @RequestMapping("/guild/{guildId}")
    public UsageResponse<GuildStatisticResponse> getStatisticsFromGuild(@PathVariable long guildId, @RequestParam String token) {
        if(authorizationService.isUnauthorizedToCallApi(token)) {
            throw new UnauthorizedException("Invalid token!");
        }
        log.info("Gathering command usage statistics from guild with ID '{}'", guildId);
        return commandUsageService.getStatisticsFromGuild(guildId);
    }

    @GetMapping
    @RequestMapping("/command/{commandName}")
    public UsageResponse<MonthlyCommandUsageResponse> getStatisticFromCommand(@PathVariable String commandName, @RequestParam String token) {
        if(authorizationService.isUnauthorizedToCallApi(token)) {
            throw new UnauthorizedException("Invalid token!");
        }
        log.info("Gather command usage statistics of command '{}'", commandName);
        return commandUsageService.getCommandStatistics(commandName);
    }
}
