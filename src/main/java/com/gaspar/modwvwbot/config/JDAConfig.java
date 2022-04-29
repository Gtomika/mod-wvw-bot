package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.services.LogUploadWatcherService;
import com.gaspar.modwvwbot.services.ChannelCommandsService;
import com.gaspar.modwvwbot.services.RoleCommandsService;
import com.gaspar.modwvwbot.services.WvwRaidService;
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

    private final ChannelCommandsService watchedChannelCommandService;
    private final LogUploadWatcherService logUploadWatcherService;
    private final RoleCommandsService wvwRoleCommandService;
    private final WvwRaidService wvwRaidService;

    @Bean
    public JDA provideJDA() throws LoginException {
        return JDABuilder.createDefault(discordToken)
                .addEventListeners(watchedChannelCommandService, logUploadWatcherService,
                    wvwRoleCommandService, wvwRaidService)
                .setActivity(Activity.playing("Guild Wars 2 WvW"))
                .build();
    }

}
