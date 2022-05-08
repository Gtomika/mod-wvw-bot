package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.gw2api.ItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service that queries an accounts material storage though the Gw2 API.
 */
@Service
@Slf4j
public class Gw2StorageService {

    private final RestTemplate restTemplate;

    public Gw2StorageService(@Qualifier("gw2api") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Counts the items of interest in the material storage of an account.
     * @param apiKey API key.
     * @param amounts Items of interest and how many of them are found. This list is modified.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key has no permissions.
     */
    public void countItemsInStorage(String apiKey, List<Amount> amounts) throws Gw2ApiException, UnauthorizedException {
        String storageUrl = "/v2/account/materials?access_token=" + apiKey;
        var response = restTemplate.getForEntity(storageUrl, ItemResponse[].class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        AmountUtils.countItemArray(amounts, response.getBody());
    }

}
