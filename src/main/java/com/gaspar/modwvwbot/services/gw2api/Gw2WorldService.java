package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.HomeWorldNotFoundException;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Queries GW2 API: /worlds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Gw2WorldService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Query the API for a home world.
     * @param name Name of the world.
     * @return {@link HomeWorldResponse}.
     * @throws Gw2ApiException In case of GW2 api failed to answer.
     * @throws HomeWorldNotFoundException If this world was not found.
     */
    public HomeWorldResponse fetchHomeWorldByName(String name) throws Gw2ApiException, HomeWorldNotFoundException {
        String getAllWorldsEndpoint = apiBaseUrl + "/v2/worlds?ids=all";
        log.debug("GW2 API endpoint for fetching all worlds is: {}", getAllWorldsEndpoint);

        var response = restTemplate.getForEntity(getAllWorldsEndpoint, HomeWorldResponse[].class);
        if(response.getStatusCode() != HttpStatus.OK) {
            log.warn("GW2 API failed to respond to fetching all worlds, status was '{}'", response.getStatusCodeValue());
            throw new Gw2ApiException("Failed to fetch worlds from /worlds?ids=all");
        }
        HomeWorldResponse[] homeWorlds = response.getBody();
        if(homeWorlds == null) {
            log.warn("GW2 API failed to send world data, empty response.");
            throw new Gw2ApiException("No response when fetching worlds from /worlds?ids=all");
        }
        for(HomeWorldResponse homeWorld: homeWorlds) {
            if(homeWorld.getName().equals(name)) {
                return homeWorld;
            }
        }
        throw new HomeWorldNotFoundException("No world with name: " + name);
    }

    /**
     * Query the GW2 API for one home world with the given id.
     * @throws Gw2ApiException If the API failed to respond.
     * @throws HomeWorldNotFoundException If there is no world with this id.
     */
    public HomeWorldResponse fetchHomeWorldById(@NonNull Integer id) throws Gw2ApiException, HomeWorldNotFoundException {
        String getByIdEndpoint = apiBaseUrl + "/v2/worlds/" + id;
        log.debug("GW2 API endpoint for getting world with id '{}' is: {}", id, getByIdEndpoint);

        var response = restTemplate.getForEntity(getByIdEndpoint, HomeWorldResponse.class);
        if(response.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.info("No world with ID '{}' exists.", id);
            throw new HomeWorldNotFoundException("NO world with " + id + " exists.");
        }
        if(response.getStatusCode() != HttpStatus.OK) {
            log.warn("GW2 API failed to return data about world with ID '{}', status code: {}", id, response.getStatusCodeValue());
            throw new Gw2ApiException("Failed to query world with id " + id);
        }
        return response.getBody();
    }

}
