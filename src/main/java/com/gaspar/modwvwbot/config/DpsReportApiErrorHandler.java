package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.exception.DpsReportApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Defines what is considered na error when calling dps.report API.
 * @see com.gaspar.modwvwbot.exception.DpsReportApiException
 */
@Component
@Slf4j
public class DpsReportApiErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() != HttpStatus.OK;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        log.error("Dps.report API failed to respond, status code: {}, message: {}", response.getRawStatusCode(), response.getStatusText());
        throw new DpsReportApiException("Dps.report API error, status: " + response.getRawStatusCode());
    }
}
