package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * Configure Java Discord API object.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JDAConfig {

    @Value("${com.gaspar.modwvwbot.discord_token}")
    private String discordToken;

    /**
     * These are the API events that the bot is interested in. Slash commands don't need an intent.
     */
    private final List<GatewayIntent> gatewayIntents = List.of(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);

    private final ChannelCommandsService watchedChannelCommandService;
    private final LogUploadWatcherService logUploadWatcherService;
    private final RoleCommandsService wvwRoleCommandService;
    private final WvwRaidService wvwRaidService;
    private final PrivateMessageResponderService privateMessageResponderService;
    private final HomeWorldCommandService homeWorldCommandService;

    @Bean
    public JDA provideJDA() throws LoginException {
        log.info("Initializing JDA with the following gateway intents: {}", gatewayIntents);
        return JDABuilder.create(discordToken, gatewayIntents)
                .addEventListeners(watchedChannelCommandService, logUploadWatcherService,
                        wvwRoleCommandService, wvwRaidService, privateMessageResponderService,
                        homeWorldCommandService)
                .setActivity(Activity.playing("WvW"))
                .build();
    }

}
