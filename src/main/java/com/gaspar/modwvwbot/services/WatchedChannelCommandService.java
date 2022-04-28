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

    private final WatchedChannelRepository watchedChannelRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WATCH_CHANNEL_COMMAND)) {

            if(event.getGuild() == null) {
                log.error("Slash command '/watch_channel' must be sent from a guild.");
                event.reply("Ezt a parancsot csak szerverről lehet küldeni.").queue();
                return;
            }

            log.info("Received /watch_channel command from user '{}' in guild {}", event.getUser().getName(), event.getGuild());

            var optionAction = event.getOption(OPTION_ACTION);
            if(optionAction == null) {
                log.warn("Required option 'action' was null when processing '/watch_channel' command.");
                event.reply("Hiba: az 'action' értéket meg kell adni.").queue();
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
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy 'list' lehet.").queue();
            }
        }
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
        if(optional.isEmpty()) {
            var watchedChannel = new WatchedChannel(event.getGuild().getIdLong(), channelId);
            watchedChannelRepository.save(watchedChannel);
            log.info("Watching new channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            event.reply("Mostantól figyelem a <#" + channelId + "> nevű csatornát.").queue();
        } else {
            event.reply("Ezt a csatornát már figyelem.").queue();
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
            event.reply("Mostantól NEM figyelem a <#" + channelId + "> nevű csatornát.").queue();
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
}
