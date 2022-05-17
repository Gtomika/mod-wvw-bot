package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.gw2api.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Queries Gw2 API endpoint of the account wallet.
 */
@Service
@Slf4j
public class Gw2WalletService {

    private final RestTemplate restTemplate;

    public Gw2WalletService(@Qualifier("gw2api") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Count some currencies in the account walled.
     * @param apiKey API key.
     * @param amounts Currencies of interest.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key has no permissions.
     */
    public void countCurrenciesInWallet(String apiKey, List<Amount> amounts) throws Gw2ApiException, UnauthorizedException {
        String walletUrl = "/v2/account/wallet?access_token=" + apiKey;
        try {
            var response = restTemplate.getForEntity(walletUrl, CurrencyResponse[].class);
            if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
            AmountUtils.countCurrencyArray(amounts, response.getBody());
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
        }
    }
}
