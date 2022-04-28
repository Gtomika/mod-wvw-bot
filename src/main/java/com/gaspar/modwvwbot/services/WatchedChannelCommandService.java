package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.model.WatchedChannel;
import com.gaspar.modwvwbot.repository.WatchedChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the watched channels command (/watch_channel).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WatchedChannelCommandService extends ListenerAdapter {

    private static final String WATCH_CHANNEL_COMMAND = "/watch_channel";

    private static final String OPTION_ACTION = "action";

    private static final String OPTION_CHANNEL = "channel_name";

    private final ErrorResponderService errorResponderService;
    private final WatchedChannelRepository watchedChannelRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WATCH_CHANNEL_COMMAND)) {

            if(event.getGuild() == null) {
                log.error("Slash command '/watch_channel' must be sent from a guild.");
                errorResponderService.sendSlashCommandErrorResponse(event, "Ezt a parancsot csak szerverről lehet küldeni.");
                return;
            }

            log.info("Received /watch_channel command from user '{}' in guild {}", event.getUser().getName(), event.getGuild());

            var optionAction = event.getOption(OPTION_ACTION);
            if(optionAction == null) {
                log.error("Required option 'action' was null when processing '/watch_channel' command.");
                errorResponderService.sendSlashCommandErrorResponse(event, "Hiba: az 'action' értéket meg kell adni.");
                return;
            }

            switch (optionAction.getAsString()) {
                case "watched_channel_add":
                    onAddWatchedChannel(event);
                    break;
                case "watched_channel_delete":
                    onDeleteWatchedChannel(event);
                    break;
                case "watched_channel_list":
                    onListWatchedChannels(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    errorResponderService.sendSlashCommandErrorResponse(event, "Nem megengedett érték: csak 'add' vagy 'delete' lehet.");
            }
        }
    }

    @Nullable
    private String getChannelName(SlashCommandInteractionEvent event) {
        var optionChannel = event.getOption(OPTION_CHANNEL);
        if(optionChannel == null) {
            errorResponderService.sendSlashCommandErrorResponse(event, "Hiba: a 'channel_name' értéknek meg kell adni egy szöveges csatornát.");
            return null;
        }

        TextChannel textChannel = optionChannel.getAsTextChannel();
        if(textChannel == null) {
            errorResponderService.sendSlashCommandErrorResponse(event, "Hiba: Szöveges csatornát kell megadni.");
            return null;
        }
        log.debug("Channel name of /watch_channel target is '{}'", textChannel.getName());
        return textChannel.getName();
    }

    private void onAddWatchedChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        String channelName = getChannelName(event);
        if(channelName == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelName(guildId, channelName);
        if(optional.isEmpty()) {
            var watchedChannel = new WatchedChannel(event.getGuild().getIdLong(), event.getChannel().getName());
            watchedChannelRepository.save(watchedChannel);
            log.info("Watching new channel '{}' on guild '{}'", channelName, event.getGuild().getName());
            event.reply("Mostantól figyelem a " + channelName + " nevű csatornát.").queue();
        } else {
            event.reply("Ezt a csatornát már figyelem.").queue();
        }
    }

    private void onDeleteWatchedChannel(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        String channelName = getChannelName(event);
        if(channelName == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelName(guildId, channelName);
        if(optional.isPresent()) {
            watchedChannelRepository.delete(optional.get());
            log.info("No longer watching channel '{}' on guild '{}'", channelName, event.getGuild().getName());
            event.reply("Mostantól NEM figyelem a " + channelName + " nevű csatornát.").queue();
        } else {
            event.reply("Ezt a csatornát nem is figyeltem.").queue();
        }
    }

    private void onListWatchedChannels(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        List<String> watchedChannels = getWatchedChannelNames(guildId);
        log.info("Listed the following channels on guild '{}': {}", event.getGuild().getName(), watchedChannels);
        if(watchedChannels.isEmpty()) {
            event.reply("Nem figyelek egyetlen csatonát sem ezen a szerveren.").queue();
        } else {
            event.reply("Ezeket a csatornákat figyelem a szerveren: " + watchedChannels).queue();
        }
    }

    public List<String> getWatchedChannelNames(long guildId) {
        return watchedChannelRepository.getByGuildId(guildId)
                .stream()
                .map(WatchedChannel::getChannelName)
                .collect(Collectors.toList());
    }

    public boolean isWatchedChannel(long guildId, Channel channel) {
        var watched = getWatchedChannelNames(guildId);
        return watched.contains(channel.getName());
    }
}
