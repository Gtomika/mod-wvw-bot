package com.gaspar.modwvwbot.services.gw2api;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.misc.TimeUtils;
import com.gaspar.modwvwbot.model.matchup.WvwColor;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupReport;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupResponse;
import com.gaspar.modwvwbot.model.matchup.WvwMatchupSide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * Creates a {@link WvwMatchupReport} of the current matchup of a world.
     * @param homeWorldId ID of the world.
     * @return Report.
     * @throws Gw2ApiException If the API failed to answer.
     */
    public WvwMatchupReport createMatchupReport(int homeWorldId) throws Gw2ApiException {
        String matchupUrl = "/v2/wvw/matches?world=" + homeWorldId;
        log.debug("Getting Wvw matchup report from: {}", matchupUrl);
        var response = restTemplate.getForEntity(matchupUrl, WvwMatchupResponse.class);
        if(response.getBody() == null) throw new Gw2ApiException("Response body was null!");

        var red = createSideFromResponse(response.getBody(), WvwColor.red);
        var blue = createSideFromResponse(response.getBody(), WvwColor.blue);
        var green = createSideFromResponse(response.getBody(), WvwColor.green);
        LocalDateTime endTime = LocalDateTime.parse(response.getBody().getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
        //for some reason we need to offset. probably time zone difference between gw2 api and hungarian time
        endTime = endTime.plusHours(TimeUtils.getHourOffset());
        return new WvwMatchupReport(List.of(red, blue, green), endTime);
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
    private List<String> getWorldNames(List<Integer> worldIds) {
        return worldIds.stream()
                .map(id -> gw2WorldService.fetchHomeWorldById(id).getName())
                .collect(Collectors.toList());
    }

}
