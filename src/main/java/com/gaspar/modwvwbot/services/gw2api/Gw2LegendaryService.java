package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.gw2api.ItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Queries gw2 api legendary items related endpoints.
 */
@Service
@Slf4j
public class Gw2LegendaryService {

    private final RestTemplate restTemplate;

    public Gw2LegendaryService(@Qualifier("gw2api") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Count how much of these legendaries the user has.
     * @param wvwLegendaries Legendaries. Will be modified during this call.
     * @param apiKey API key for the account.
     * @throws Gw2ApiException If the api failed to send response.
     * @throws UnauthorizedException If api key has no permissions.
     */
    public void countLegendaries(List<Amount> wvwLegendaries, String apiKey) throws Gw2ApiException, UnauthorizedException {
        String legendaryEndpoint = "/v2/account/legendaryarmory?access_token=" + apiKey;
        try {
            var response = restTemplate.getForEntity(legendaryEndpoint, ItemResponse[].class);
            if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
            AmountUtils.countItemArray(wvwLegendaries, response.getBody());
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
        }
    }
}
