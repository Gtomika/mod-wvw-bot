package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.services.LogUploadWatcherService;
import com.gaspar.modwvwbot.services.WatchedChannelCommandService;
import com.gaspar.modwvwbot.services.WvwRoleCommandService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

/**
 * Configure Java Discord API object.
 */
@Configuration
@RequiredArgsConstructor
public class JDAConfig {

    @Value("${com.gaspar.modwvwbot.discord_token}")
    private String discordToken;

    private final WatchedChannelCommandService watchedChannelCommandService;
    private final LogUploadWatcherService logUploadWatcherService;
    private final WvwRoleCommandService wvwRoleCommandService;

    @Bean
    public JDA provideJDA() throws LoginException {
        return JDABuilder.createDefault(discordToken)
                .addEventListeners(watchedChannelCommandService, logUploadWatcherService,
                    wvwRoleCommandService)
                .setActivity(Activity.playing("Guild Wars 2 WvW"))
                .build();
    }

}
