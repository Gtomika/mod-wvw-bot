package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Performs operations on the Gw2 API related to characters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Gw2CharacterService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Fetch all character names of an account.
     * @param apiKey API key.
     * @return List of names (not encoded).
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key does not have character permission.
     */
    public List<String> fetchCharacterNames(String apiKey) throws Gw2ApiException, UnauthorizedException {
        String getCharactersUrl = apiBaseUrl + "/v2/characters?access_token=" + apiKey;
        var response = restTemplate.getForEntity(getCharactersUrl, String[].class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null.");
        return Arrays.asList(response.getBody());

    }
}
