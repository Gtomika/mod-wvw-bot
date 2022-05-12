package com.gaspar.modwvwbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configures rest templates used by the bot. There is one for every API used.
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String gw2ApiBaseUrl;

    private final Gw2ApiErrorHandler gw2ApiErrorHandler;

    @Bean
    @Qualifier("gw2api")
    public RestTemplate provideGw2ApiRestTemplate() {
        return new RestTemplateBuilder()
                .errorHandler(gw2ApiErrorHandler)
                .rootUri(gw2ApiBaseUrl)
                .build();
    }

}
