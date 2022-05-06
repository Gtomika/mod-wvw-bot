package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.services.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configure slash command handlers.
 */
@Configuration
@RequiredArgsConstructor
public class SlashCommandConfig {

    private final ChannelCommandsService channelCommandsService;
    private final RoleCommandsService roleCommandsService;
    private final HomeWorldCommandService homeWorldCommandService;
    private final WvwItemsService wvwItemsService;
    private final WvwCurrenciesService wvwCurrenciesService;
    private final WvwRaidService wvwRaidService;

    @AllArgsConstructor
    @Getter
    public static class SlashCommandHandlers {
        private List<SlashCommandHandler> handlerList;
    }

    @Bean
    public SlashCommandHandlers provideHandlers() {
        return new SlashCommandHandlers(List.of(
                channelCommandsService,
                roleCommandsService,
                homeWorldCommandService,
                wvwItemsService,
                wvwCurrenciesService,
                wvwRaidService
        ));
    }

}