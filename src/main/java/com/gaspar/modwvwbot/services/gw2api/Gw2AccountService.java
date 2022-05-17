package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.model.gw2api.Gw2Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Sends request to API to protected endpoint, to check if
 * API key is correct.
 */
@Service
@Slf4j
public class Gw2AccountService {

    private final RestTemplate restTemplate;

    public Gw2AccountService(@Qualifier("gw2api") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get username of player.
     * @param apiKey API key provided.
     * @return {@link Gw2Account}.
     * @throws Gw2ApiException If the api failed to send response.
     * @throws UnauthorizedException If api key is invalid.
     */
    public Gw2Account fetchGw2User(String apiKey) throws Gw2ApiException, UnauthorizedException {
        String getUserEndpoint = "/v2/account";
        log.debug("Fetching Gw2 account data from: " + getUserEndpoint);
        getUserEndpoint += "?access_token=" + apiKey;
        try {
            var response = restTemplate.getForEntity(getUserEndpoint, Gw2Account.class);
            if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
        }
    }
}
