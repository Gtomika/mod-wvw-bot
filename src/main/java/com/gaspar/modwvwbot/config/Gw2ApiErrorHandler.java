package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Tells a {@link org.springframework.web.client.RestTemplate} how to handle
 * errors from and what exceptions to throw from gw2 api responses.
 */
@Component
@Slf4j
public class Gw2ApiErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(@NonNull ClientHttpResponse response) throws IOException {
        if(response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.info("API key has no required permissions, GW2 API responded with 403.");
            throw new UnauthorizedException("APi key does not have required permissions");
        } else {
            log.warn("Gw2 API failed to respond, status text: {}, code: {}", response.getStatusText(), response.getRawStatusCode());
            throw new Gw2ApiException("Gw2 API failed to answer, status: " + response.getRawStatusCode());
        }
    }
}
