package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.controllers.dto.AnnouncementRequest;
import com.gaspar.modwvwbot.controllers.dto.AnnouncementResponse;
import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.model.AnnouncementChannel;
import com.gaspar.modwvwbot.model.WatchedChannel;
import com.gaspar.modwvwbot.repository.AnnouncementChannelRepository;
import com.gaspar.modwvwbot.repository.WatchedChannelRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the watched channels command (/watch_channel) and the announcement channel (/announcement_channel) command.
 * These are handled together because they are very similar.
 */
@Service
@Slf4j
public class ChannelCommandsService implements SlashCommandHandler {

    private static final String WATCH_CHANNEL_COMMAND = "/watch_channel";

    private static final String ANNOUNCEMENT_CHANNEL_COMMAND = "/announcement_channel";

    private static final String OPTION_ACTION = "action";

    private static final String OPTION_CHANNEL = "channel_name";

    private final WatchedChannelRepository watchedChannelRepository;
    private final AnnouncementChannelRepository announcementChannelRepository;
    private final AuthorizationService authorizationService;
    private final JDA jda;

    public ChannelCommandsService(WatchedChannelRepository watchedChannelRepository,
                                  AnnouncementChannelRepository announcementChannelRepository,
                                  AuthorizationService authorizationService,
                                  @Lazy JDA jda) {
        this.watchedChannelRepository = watchedChannelRepository;
        this.announcementChannelRepository = announcementChannelRepository;
        this.authorizationService = authorizationService;
        this.jda = jda;
    }

    @Override
    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        var optionAction = getOptionAction(event);
        if(optionAction == null) return;
        if(event.getCommandString().startsWith(WATCH_CHANNEL_COMMAND)) {
            switch (optionAction.getAsString()) {
                case "watched_channel_add":
                    if(authorizationService.authorize(event)) onAddWatchedChannel(event);
                    break;
                case "watched_channel_delete":
                    if(authorizationService.authorize(event)) onDeleteWatchedChannel(event);
                    break;
                case "watched_channel_list":
                    onListWatchedChannels(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy 'list' lehet.").queue();
            }
        } else if(event.getCommandString().startsWith(ANNOUNCEMENT_CHANNEL_COMMAND)) {
            switch (optionAction.getAsString()) {
                case "announcement_channel_add":
                    if(authorizationService.authorize(event)) onAddAnnouncementChannel(event);
                    break;
                case "announcement_channel_delete":
                    if(authorizationService.authorize(event)) onDeleteAnnouncementChannel(event);
                    break;
                case "announcement_channel_list":
                    onListAnnouncementChannels(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy 'list' lehet.").queue();
            }
        } else {
            log.warn("Unknown command routed to Role Command Handler Service.");
            event.reply("Hiba történt. Ezt kérlek jelezd a fejlesztőnek.").queue();
        }
    }

    @Override
    public String commandName() {
        return null;
    }

    @Override
    public boolean handlesMultipleCommands() {
        return true;
    }

    @Override
    public String[] commandNames() {
        return new String[] {WATCH_CHANNEL_COMMAND, ANNOUNCEMENT_CHANNEL_COMMAND};
    }

    /**
     * Extract command option "action" from the request.
     * @return Option or null if it could not be extracted.
     */
    private OptionMapping getOptionAction(SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            log.error("Slash command must be sent from a guild.");
            event.reply("Ezt a parancsot csak szerverről lehet küldeni.").queue();
            return null;
        }

        log.info("Received command '{}' from user '{}' in guild {}", event.getCommandString(), event.getUser().getName(), event.getGuild());

        var optionAction = event.getOption(OPTION_ACTION);
        if(optionAction == null) {
            log.debug("Required option 'action' was null when processing '{}' command.", event.getCommandString());
            event.reply("Hiba: az 'action' értéket meg kell adni.").queue();
            return null;
        }
        return optionAction;
    }

    @Nullable
    private Long getTargetChannelId(SlashCommandInteractionEvent event) {
        var optionChannel = event.getOption(OPTION_CHANNEL);
        if(optionChannel == null) {
            event.reply("Hiba: a 'channel_name' értéknek meg kell adni egy szöveges csatornát.").queue();
            return null;
        }

        TextChannel textChannel = optionChannel.getAsTextChannel();
        if(textChannel == null) {
            event.reply("Hiba: Szöveges csatornát kell megadni.").queue();
            return null;
        }
        log.debug("Channel name of /watch_channel target is '{}'", textChannel.getName());
        return textChannel.getIdLong();
    }

    private void onAddWatchedChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event);
        if(channelId == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        String emote = EmoteUtils.defaultEmote("eyes");
        if(optional.isEmpty()) {
            var watchedChannel = new WatchedChannel(event.getGuild().getIdLong(), channelId);
            watchedChannelRepository.save(watchedChannel);
            log.info("Watching new channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            event.reply("Mostantól figyelem a <#" + channelId + "> csatornát " + emote).queue();
        } else {
            event.reply("Ezt a csatornát már figyelem " + emote).queue();
        }
    }

    private void onDeleteWatchedChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event);
        if(channelId == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        if(optional.isPresent()) {
            watchedChannelRepository.delete(optional.get());
            log.info("No longer watching channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            event.reply("Mostantól NEM figyelem a <#" + channelId + "> csatornát.").queue();
        } else {
            event.reply("Ezt a csatornát nem is figyeltem.").queue();
        }
    }

    private void onListWatchedChannels(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        List<String> watchedChannels = getWatchedChannelNames(guildId);
        log.info("Listed the following watched channels on guild '{}': {}", event.getGuild().getName(), watchedChannels);

        if(watchedChannels.isEmpty()) {
            String emote = EmoteUtils.defaultEmote("sleeping");
            event.reply("Nem figyelek egyetlen csatonát sem ezen a szerveren " + emote).queue();
        } else {
            String emote = EmoteUtils.defaultEmote("eyes");
            event.reply("Ezeket a csatornákat figyelem " + emote + " a szerveren: " + watchedChannels).queue();
        }
    }

    /**
     * Get list of watched channel names, formatted to appear as links in discord.
     */
    public List<String> getWatchedChannelNames(long guildId) {
        return watchedChannelRepository.getByGuildId(guildId)
                .stream()
                .map(w -> "<#" + w.getChannelId() + ">")
                .collect(Collectors.toList());
    }

    /**
     * Decides if a channel is watched in a guild.
     */
    public boolean isWatchedChannel(long guildId, Channel channel) {
        for(var watchedChannel: watchedChannelRepository.getByGuildId(guildId)) {
            if(watchedChannel.getChannelId() == channel.getIdLong()) {
                return true;
            }
        }
        return false;
    }

    private void onAddAnnouncementChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event);
        if(channelId == null) return;

        var optional = announcementChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        String emote = EmoteUtils.defaultEmote("mega");
        if(optional.isEmpty()) {
            var announcementChannel = new AnnouncementChannel(event.getGuild().getIdLong(), channelId);
            announcementChannelRepository.save(announcementChannel);
            log.info("New announcement channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            event.reply("A hirdetéseimet mostantól <#" + channelId + "> csatornára is kirakom " + emote).queue();
        } else {
            event.reply("Erre a csatornára már most is írok hirdetéseket " + emote).queue();
        }
    }

    private void onDeleteAnnouncementChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event);
        if(channelId == null) return;

        var optional = announcementChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        if(optional.isPresent()) {
            announcementChannelRepository.delete(optional.get());
            log.info("Channel with id '{}' on guild '{}' is no longer an announcement channel.", channelId, event.getGuild().getName());
            event.reply("Mostantól NEM használom hirdetésekre <#" + channelId + "> csatornát.").queue();
        } else {
            event.reply("Ezt a csatornát nem is használtam hirdetésre.").queue();
        }
    }

    private void onListAnnouncementChannels(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        List<String> announcementChannels = getAnnouncementChannelNames(guildId);
        log.info("Listed the following announcement channels on guild '{}': {}", event.getGuild().getName(), announcementChannels);
        if(announcementChannels.isEmpty()) {
            event.reply("Nem hirdetek egyetlen csatonán sem ezen a szerveren.").queue();
        } else {
            String emote = EmoteUtils.defaultEmote("mega");
            event.reply("Ezeken a csatornákon hirdetek " + emote + " a szerveren: " + announcementChannels).queue();
        }
    }

    /**
     * Get formatted names (clickable in chat) for announcement channels in a guild.
     */
    private List<String> getAnnouncementChannelNames(long guildId) {
        return announcementChannelRepository.getByGuildId(guildId)
                .stream()
                .map(a -> "<#" + a.getChannelId() + ">")
                .collect(Collectors.toList());
    }

    public List<Long> getAnnouncementChannels(long guildId) {
        return announcementChannelRepository.getByGuildId(guildId)
                .stream()
                .map(AnnouncementChannel::getChannelId)
                .collect(Collectors.toList());
    }

    /**
     * Make announcements in guilds.
     * @param request Request with details.
     * @return Response with details.
     */
    public AnnouncementResponse publishAnnouncements(AnnouncementRequest request) {
        log.info("Posting announcement according to request: {}", request);

        int postChannelCount = 0, failChannelCount = 0;
        Set<Long> postedGuildIds = new HashSet<>();
        for(AnnouncementChannel channel: announcementChannelRepository.findAll()) {
            if(request.getGuildIds() == null || request.getGuildIds().contains(channel.getGuildId())) {
                //post on this channel
                TextChannel textChannel = jda.getTextChannelById(channel.getChannelId());
                if(textChannel != null && textChannel.canTalk()) {
                    postChannelCount++;
                    textChannel.sendMessage(request.getMessage()).queue();
                } else {
                    failChannelCount++;
                }
                postedGuildIds.add(channel.getChannelId());
            }
        }

        var response = new AnnouncementResponse(
                "Announcement successfully posted",
                postChannelCount,
                failChannelCount,
                postedGuildIds.size());
        log.info("Announcements posted, result is: {}", response);
        return response;
    }
}
