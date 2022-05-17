package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.HomeWorldNotFoundException;
import com.gaspar.modwvwbot.model.gw2api.HomeWorldResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Queries GW2 API: /worlds
 */
@Service
@Slf4j
public class Gw2WorldService {

    private final RestTemplate restTemplate;

    public Gw2WorldService(@Qualifier("gw2api") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Query the API for a home world.
     * @param name Name of the world.
     * @return {@link HomeWorldResponse}.
     * @throws Gw2ApiException In case of GW2 api failed to answer.
     * @throws HomeWorldNotFoundException If this world was not found.
     */
    public HomeWorldResponse fetchHomeWorldByName(String name) throws Gw2ApiException, HomeWorldNotFoundException {
        String getAllWorldsEndpoint = "/v2/worlds?ids=all";
        log.debug("GW2 API endpoint for fetching all worlds is: {}", getAllWorldsEndpoint);

        ResponseEntity<HomeWorldResponse[]> response;
        try {
            response = restTemplate.getForEntity(getAllWorldsEndpoint, HomeWorldResponse[].class);
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
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
     * Query the GW2 API for one home world with the given VALID id.
     * @throws Gw2ApiException If the API failed to respond.
     */
    public HomeWorldResponse fetchHomeWorldById(@NonNull Integer id) throws Gw2ApiException {
        String getByIdEndpoint = "/v2/worlds/" + id;
        log.debug("GW2 API endpoint for getting world with id '{}' is: {}", id, getByIdEndpoint);
        try {
            var response = restTemplate.getForEntity(getByIdEndpoint, HomeWorldResponse.class);
            if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
        }
    }

}
