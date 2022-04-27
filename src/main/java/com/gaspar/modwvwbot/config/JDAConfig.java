package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.services.EventsListenerService;
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

    private final EventsListenerService eventsListenerService;

    @Bean
    public JDA provideJDA() throws LoginException {
        return JDABuilder.createDefault(discordToken)
                .addEventListeners(eventsListenerService)
                .setActivity(Activity.playing("Guild Wars 2"))
                .build();
    }

}
