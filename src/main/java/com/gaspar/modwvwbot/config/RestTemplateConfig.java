package com.gaspar.modwvwbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final Gw2ApiErrorHandler errorHandler;

    @Bean
    public RestTemplate provideRestTemplate() {
        return new RestTemplateBuilder()
                .errorHandler(errorHandler)
                .build();
    }

}
