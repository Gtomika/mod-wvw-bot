package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.SlashCommandDispatcher;
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

    private final LogUploadWatcherService logUploadWatcherService;
    private final PrivateMessageResponderService privateMessageResponderService;
    private final GeneralMessageResponderService generalMessageResponderService;
    private final SlashCommandDispatcher slashCommandDispatcher;

    @Bean
    public JDA provideJDA() throws LoginException {
        log.info("Initializing JDA with the following gateway intents: {}", gatewayIntents);
        return JDABuilder.create(discordToken, gatewayIntents)
                .addEventListeners(logUploadWatcherService,
                        privateMessageResponderService,
                        generalMessageResponderService,
                        slashCommandDispatcher)
                .setActivity(Activity.playing("WvW"))
                .build();
    }

}
