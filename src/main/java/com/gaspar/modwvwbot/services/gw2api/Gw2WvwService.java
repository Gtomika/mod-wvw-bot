package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.model.gw2api.WvwRank;
import com.gaspar.modwvwbot.model.matchup.WvwColor;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupReport;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupResponse;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupSide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Queries Gw2 API wvw endpoint.
 */
@Service
@Slf4j
public class Gw2WvwService {

    private final RestTemplate restTemplate;
    private final Gw2WorldService gw2WorldService;

    public Gw2WvwService(@Qualifier("gw2api") RestTemplate restTemplate, Gw2WorldService gw2WorldService) {
        this.restTemplate = restTemplate;
        this.gw2WorldService = gw2WorldService;
    }

    /**
     * Creates a {@link WvwMatchupReport} from a matchup ID.
     */
    public WvwMatchupReport createMatchupReport(String matchupId) {
        String matchupUrl = "/v2/wvw/matches/" + matchupId;
        return getMatchupReport(matchupUrl);
    }

    /**
     * Creates a {@link WvwMatchupReport} of the current matchup of a world.
     * @param homeWorldId ID of the world.
     * @return Report.
     * @throws Gw2ApiException If the API failed to answer.
     */
    public WvwMatchupReport createMatchupReport(int homeWorldId) throws Gw2ApiException {
        String matchupUrl = "/v2/wvw/matches?world=" + homeWorldId;
        return getMatchupReport(matchupUrl);
    }

    /**
     * Fetches a wvw matchup report.
     * @param matchupUrl URL used to get the report.
     */
    private WvwMatchupReport getMatchupReport(String matchupUrl) {
        log.debug("Getting Wvw matchup report from: {}", matchupUrl);
        ResponseEntity<WvwMatchupResponse> response;
        try {
            response = restTemplate.getForEntity(matchupUrl, WvwMatchupResponse.class);
            if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        } catch (ResourceAccessException e) {
            log.error("Gw2 API failure.", e);
            throw new Gw2ApiException(e);
        }

        var red = createSideFromResponse(response.getBody(), WvwColor.red);
        var blue = createSideFromResponse(response.getBody(), WvwColor.blue);
        var green = createSideFromResponse(response.getBody(), WvwColor.green);
        //get tier from ID
        String id = response.getBody().getMatchId();
        int tier = Integer.parseInt(id.split("-")[1]);
        return new WvwMatchupReport(List.of(red, blue, green), tier);
    }

    /**
     * Extract details of one side from the response.
     * @param response API matchup response.
     * @param color Color of the side.
     * @throws Gw2ApiException If API failed to return world data.
     */
    private WvwMatchupSide createSideFromResponse(WvwMatchupResponse response, WvwColor color) throws Gw2ApiException {
        //names of the worlds (main world is the last)
        var names = getWorldNames(response.getAllWorlds().getByColor(color));
        int kills = response.getKills().getByColor(color);
        int deaths = response.getDeaths().getByColor(color);
        float kdRatio = (float)kills / deaths;

        int victoryPoints = response.getVictoryPoints().getByColor(color);

        return WvwMatchupSide.builder()
                .color(color)
                .worldNames(names)
                .worldIds(response.getAllWorlds().getByColor(color))
                .killCount(kills)
                .deathCount(deaths)
                .victoryPoints(victoryPoints)
                .killDeathRatio(kdRatio)
                .build();
    }

    /**
     * Get a list of world names based on world IDs.
     * @param worldIds World IDs.
     */
    private List<String> getWorldNames(List<Integer> worldIds) throws Gw2ApiException {
        return worldIds.stream()
                .map(id -> gw2WorldService.fetchHomeWorldById(id).getName())
                .collect(Collectors.toList());
    }

    /**
     * Get all {@link WvwRank}s from the API.
     */
    public WvwRank[] getWvwRanks() throws Gw2ApiException {
        String endpoint = "/v2/wvw/ranks?ids=all";
        var response = restTemplate.getForEntity(endpoint, WvwRank[].class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");
        return response.getBody();
    }

}
