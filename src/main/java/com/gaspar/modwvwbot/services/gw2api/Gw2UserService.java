package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.model.gw2api.Gw2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Sends request to API to protected endpoint, to check if
 * API key is correct.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Gw2UserService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Get username of player.
     * @param apiKey API key provided.
     * @return {@link Gw2User}.
     * @throws Gw2ApiException If the api failed to send response.
     * @throws UnauthorizedException If api key is invalid.
     */
    public Gw2User fetchGw2User(String apiKey) throws Gw2ApiException, UnauthorizedException {
        String getUserEndpoint = apiBaseUrl + "/v2/account";
        log.debug("Fetching Gw2 account data from: " + getUserEndpoint);
        getUserEndpoint += "?access_token=" + apiKey;
        var response = restTemplate.getForEntity(getUserEndpoint, Gw2User.class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        return response.getBody();
    }
}
