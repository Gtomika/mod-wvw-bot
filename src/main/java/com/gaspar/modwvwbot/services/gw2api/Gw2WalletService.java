package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.gw2api.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Queries Gw2 API endpoint of the account wallet.
 */
@Service
@RequiredArgsConstructor
public class Gw2WalletService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Count some currencies in the account walled.
     * @param apiKey API key.
     * @param amounts Currencies of interest.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key has no permissions.
     */
    public void countCurrenciesInWallet(String apiKey, List<Amount> amounts) throws Gw2ApiException, UnauthorizedException {
        String walletUrl = apiBaseUrl + "/v2/account/wallet?access_token=" + apiKey;
        var response = restTemplate.getForEntity(walletUrl, CurrencyResponse[].class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        AmountUtils.countCurrencyArray(amounts, response.getBody());
    }
}
