package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.gw2api.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Queries the Gw2 API for the contents of the in game bank of an account.
 */
@Service
@RequiredArgsConstructor
public class Gw2BankService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Counts the items of interest in the bank of an account.
     * @param apiKey API key.
     * @param amounts Items of interest and how many of them are found. This list is modified.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key has no permissions.
     */
    public void countItemsInBank(String apiKey, List<Amount> amounts) throws Gw2ApiException, UnauthorizedException {
        String bankUrl = apiBaseUrl + "/v2/account/bank?access_token=" + apiKey;
        //in the response, it will be a JSON array or item responses. bank tabs are not separated
        var response = restTemplate.getForEntity(bankUrl, ItemResponse[].class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        AmountUtils.countItemArray(amounts, response.getBody());
    }

}
