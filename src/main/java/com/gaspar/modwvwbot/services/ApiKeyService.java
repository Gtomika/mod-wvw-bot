package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.exception.Gw2ApiException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.ApiKey;
import com.gaspar.modwvwbot.model.gw2api.Gw2User;
import com.gaspar.modwvwbot.repository.ApiKeyRepository;
import com.gaspar.modwvwbot.services.gw2api.Gw2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Manages user interactions about API keys.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {

    /**
     * Message about API keys starts like this.
     */
    public static final String APIKEY_TAG = "modwvwbot-apikey";

    @Value("${com.gaspar.modwvwbot.gw2_api_key_url}")
    private String apiKeyUrl;

    @Value("${com.gaspar.modwvwbot.gw2_api_key_length}")
    private int apiKeyLength;

    @Value("${com.gaspar.modwvwbot.gw2_api_key_regex}")
    private String apiKeyRegex;

    @Value("${com.gaspar.modwvwbot.gw2_api_key_permissions}")
    private List<String> permissions;

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private Pattern apiKeyPattern;

    private final ApiKeyRepository apiKeyRepository;
    private final Gw2UserService gw2UserService;

    @PostConstruct
    public void init() {
        apiKeyPattern = Pattern.compile(apiKeyRegex);
    }

    /**
     * Get and store the user's API key.
     * @param event Private message that starts with {@link #APIKEY_TAG}.
     */
    public void processApiKeyMessage(MessageReceivedEvent event) {
        log.info("User '{}' is adding an API key...", event.getAuthor().getName());
        String keyString = getValidFormatApiKey(event);
        if(keyString == null) return;

        confirmApiKey(event, keyString);
    }

    /**
     * Extract the key from the message.
     * @return The key, or null if it was not valid. In this case, an error response is already sent.
     */
    @Nullable
    private String getValidFormatApiKey(MessageReceivedEvent event) {
        String keyEmote = EmoteUtils.defaultEmote("key");
        String[] parts = event.getMessage().getContentRaw().split(" ");
        if(parts.length != 2) {
            log.info("User '{}' submitted api key incorrectly: {}", event.getAuthor().getName(), event.getMessage().getContentRaw());
            event.getChannel().sendMessage("Érvénytelen formátum. Az API kulcsodat "+ keyEmote +" így tudod megadni: '" +
                    APIKEY_TAG + " [másold ide az API kulcsod]'.").queue();
            return null;
        }
        String key = parts[1];
        if(key.length() != apiKeyLength) {
            log.info("User '{}' submitted api key with incorrect length: {}", event.getAuthor().getName(), event.getMessage().getContentRaw());
            event.getChannel().sendMessage("Ez nem egy érvényes API kulcs "+ keyEmote +", azok mindig " + apiKeyLength + " karakter " +
                    "hosszúak. Másold ki onnan ahol létrehoztad, így biztos nem rontod el.").queue();
            return null;
        }
        if(!apiKeyPattern.matcher(key).matches()) {
            log.info("User '{}' submitted api key which does not match regex: {}", event.getAuthor().getName(), event.getMessage().getContentRaw());
            event.getChannel().sendMessage("Ez nem egy érvényes API "+ keyEmote +"  kulcs, azok csak angol nagybetűket, számokat és kötőjelet " +
                    "tartalmaznak. Másold ki onnan ahol létrehoztad, így biztos nem rontod el.").queue();
            return null;
        }
        //format is valid
        return key;
    }

    public void sendPrivateMessageApiKeyInfo(MessageReceivedEvent event) {
        String key = EmoteUtils.defaultEmote("key");
        event.getMessage().reply("Privát üzenetben csak az API kulcsodat "+ key +" tudod megadni. " +
                "A kulcsot így adhatod meg: '"+ ApiKeyService.APIKEY_TAG +" [másold ide az API kulcsot]'").queue();
    }

    public void sendPublicMessageApiKeyInfo(MessageReceivedEvent event) {
        String warning = EmoteUtils.defaultEmote("warning");
        String key = EmoteUtils.defaultEmote("key");
        event.getMessage().reply(warning + " Nyilvános üzenetben adtad meg az API kulcsodat! " + warning + " Ezt így nem fogadom el, és " +
                "javaslom hogy, hogy készíts egy új kulcsot "+ key +"  ehelyett, amit **privát** üzenetben küldj el nekem.").queue();
    }

    /**
     * @return List of GW2 API key permissions needed.
     */
    public List<String> getRequiredPermissions() {
        return permissions;
    }

    /**
     * Send request to gw2 API with this key, to confirm it is working and has permissions.
     * @param event User's API key add message.
     * @param keyString Key to be confirmed. Valid format is guaranteed.
     */
    private void confirmApiKey(MessageReceivedEvent event, String keyString) {
        String loading = EmoteUtils.animatedEmote("loading", loadingId);
        event.getChannel().sendMessage("Egy kis türelmet... " + loading).queue(message -> {
            try {

                var gw2User = gw2UserService.fetchGw2User(keyString);
                saveApiKey(message, event, keyString, gw2User);
            } catch (UnauthorizedException e) {
                String emote = EmoteUtils.defaultEmote("no_entry_sign");
                message.editMessage("Nem sikerült hitelesíteni ezzel a kulccsal " + emote +
                        "! Ellenőrizd, hogy jó kulcsot adtál-e meg, és megvannak rajta ezek az engedélyek: " + permissions).queue();
            } catch (Gw2ApiException e) {
                String emote = EmoteUtils.defaultEmote("no_entry_sign");
                message.editMessage("Nem sikerült elérni a GW2 API-t " + emote +
                        ". Ez nem a te hibád, próbáld újra kicsit később.").queue();
            }
        });
    }

    /**
     * Save user's API key.
     * @param message Status message that can be edited if the process completes.
     * @param event Message event.
     * @param keyString Key string, guaranteed to be valid and confirmed.
     * @param gw2User GW2 account data.
     */
    private void saveApiKey(Message message, MessageReceivedEvent event, String keyString, Gw2User gw2User) {
        String keyEmote = EmoteUtils.defaultEmote("key");
        long userId = event.getAuthor().getIdLong();
        var optional = apiKeyRepository.findByUserId(userId);
        if(optional.isPresent()) {
            ApiKey apiKey = optional.get();
            apiKey.setKey(keyString);
            apiKeyRepository.save(apiKey);
            log.info("User '{}' has updated their API key.", event.getAuthor().getName());
            message.editMessage("Elmentettem az új API kulcsodat " + keyEmote +
                    ". Eszerint te **" + gw2User.getAccountName() + "** vagy.").queue();
        } else {
            ApiKey apiKey = new ApiKey(userId, keyString);
            apiKeyRepository.save(apiKey);
            log.info("User '{}' has added their API key.", event.getAuthor().getName());
            message.editMessage("Elmentettem az API kulcsot "+ keyEmote +
                    "! Eszerint te **" + gw2User.getAccountName() + "** vagy." +
                    " Később ugyanezzel a módszerrel tudod megváltoztatni.").queue();
        }
    }

    public Optional<ApiKey> getApiKeyByUserId(long userId) {
        return apiKeyRepository.findByUserId(userId);
    }

    public String getNoApiKeyAddedMessage() {
        String key = EmoteUtils.defaultEmote("key");
        return "Még nem adtál nekem egy API kulcsot " + key + " ! Erre szükségem van, hogy " +
                "le tudjam kérni az adatokat a Gw2 szervereiről.\n" +
                " - API kulcsot itt tudsz létrehozni: " + apiKeyUrl + "\n" +
                " - Add hozzá ezeket az engedélyeket: " + permissions + "\n" +
                " - Küld el nekem a kulcsot **privát** üzenetben így: 'modwvwbot-apikey [kulcs]'";
    }

    public String getNoPermissionsMessage() {
        String error = EmoteUtils.defaultEmote("no_entry_sign");
        return "Az API kulcsod nem rendlkezik a megfelelő engedélyekkel " + error + "\n" +
                "Hozz létre és adj hozzá új kulcsot, ezekkel az engedélyekkel:\n" + permissions;
    }
 }
