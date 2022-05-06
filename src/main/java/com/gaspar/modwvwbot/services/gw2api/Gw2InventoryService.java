package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.AmountUtils;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.gw2api.Amount;
import com.gaspar.modwvwbot.model.gw2api.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service that performs Gw2 API calls to the /inventory endpoint
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Gw2InventoryService {

    @Value("${com.gaspar.modwvwbot.gw2_api_url}")
    private String apiBaseUrl;

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final RestTemplate restTemplate;
    private final Gw2CharacterService gw2CharacterService;

    /**
     * Fetches all inventories of characters and counts how many items of interest are in them.
     * @param apiKey Api key.
     * @param amounts Some amounts of items of interest. This list will be updated.
     * @param hook Used to edit the loading message.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key does not have characters or inventories permission.
     */
    public void countItemsInInventories(
            String apiKey,
            List<Amount> amounts,
            InteractionHook hook
    ) throws Gw2ApiException, UnauthorizedException {
        String loading = EmoteUtils.animatedEmote("loading", loadingId);
        hook.editOriginal("A karaktereid lekérdezése... " + loading).queue();
        log.debug("Request to count '{}' items in inventories.", amounts.size());
        var characterNames = gw2CharacterService.fetchCharacterNames(apiKey);
        log.debug("Fetched these character names from Gw2 API: {}", characterNames);
        //check inventory of all characters
        for(String name: characterNames) {
            hook.editOriginal(name + " inventory-jának vizsgálata... " + loading).queue();
            log.debug("Fetching the inventory of character '{}'...", name);
            countItemsInInventory(apiKey, name, amounts);
        }
    }

    /**
     * Count items in a characters inventory.
     * @param apiKey Api key.
     * @param name Name of the character.
     * @param amounts Amounts of items, which will be modified by this method.
     * @throws Gw2ApiException If the API fails to respond.
     * @throws UnauthorizedException If the API key does not have characters or inventories permission.
     */
    private void countItemsInInventory(String apiKey, String name, List<Amount> amounts)
            throws Gw2ApiException, UnauthorizedException {
        String getInventoryEndpoint = apiBaseUrl + "/v2/characters/%s/inventory?access_token=" + apiKey;
        String urlWithName = String.format(getInventoryEndpoint, name);
        var response = restTemplate.getForEntity(urlWithName, InventoryResponse.class);
        if(response.getBody() == null) throw new Gw2ApiException("No response body");
        AmountUtils.countInInventory(amounts, response.getBody());
    }
}
