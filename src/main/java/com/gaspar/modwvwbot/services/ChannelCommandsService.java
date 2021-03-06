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
import net.dv8tion.jda.api.interactions.InteractionHook;
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
                    event.deferReply().queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onAddWatchedChannel(event, hook);
                        }
                    });
                    break;
                case "watched_channel_delete":
                    event.deferReply().queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onDeleteWatchedChannel(event, hook);
                        }
                    });
                    break;
                case "watched_channel_list":
                    onListWatchedChannels(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett ??rt??k: csak 'add', 'delete' vagy 'list' lehet.").queue();
            }
        } else if(event.getCommandString().startsWith(ANNOUNCEMENT_CHANNEL_COMMAND)) {
            switch (optionAction.getAsString()) {
                case "announcement_channel_add":
                    event.deferReply().queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onAddAnnouncementChannel(event, hook);
                        }
                    });
                    break;
                case "announcement_channel_delete":
                    event.deferReply().queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onDeleteAnnouncementChannel(event, hook);
                        }
                    });
                    break;
                case "announcement_channel_list":
                    onListAnnouncementChannels(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett ??rt??k: csak 'add', 'delete' vagy 'list' lehet.").queue();
            }
        } else {
            log.warn("Unknown command routed to Role Command Handler Service.");
            event.reply("Hiba t??rt??nt. Ezt k??rlek jelezd a fejleszt??nek.").queue();
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
            event.reply("Ezt a parancsot csak szerverr??l lehet k??ldeni.").queue();
            return null;
        }

        log.info("Received command '{}' from user '{}' in guild {}", event.getCommandString(), event.getUser().getName(), event.getGuild());

        var optionAction = event.getOption(OPTION_ACTION);
        if(optionAction == null) {
            log.debug("Required option 'action' was null when processing '{}' command.", event.getCommandString());
            event.reply("Hiba: az 'action' ??rt??ket meg kell adni.").queue();
            return null;
        }
        return optionAction;
    }

    @Nullable
    private Long getTargetChannelId(SlashCommandInteractionEvent event, InteractionHook hook) {
        var optionChannel = event.getOption(OPTION_CHANNEL);
        if(optionChannel == null) {
            hook.editOriginal("Hiba: a 'channel_name' ??rt??knek meg kell adni egy sz??veges csatorn??t.").queue();
            return null;
        }

        TextChannel textChannel = optionChannel.getAsTextChannel();
        if(textChannel == null) {
            hook.editOriginal("Hiba: Sz??veges csatorn??t kell megadni.").queue();
            return null;
        }
        log.debug("Channel name of /watch_channel target is '{}'", textChannel.getName());
        return textChannel.getIdLong();
    }

    private void onAddWatchedChannel(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event, hook);
        if(channelId == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        String emote = EmoteUtils.defaultEmote("eyes");
        if(optional.isEmpty()) {
            var watchedChannel = new WatchedChannel(event.getGuild().getIdLong(), channelId);
            watchedChannelRepository.save(watchedChannel);
            log.info("Watching new channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            hook.editOriginal("Mostant??l figyelem a <#" + channelId + "> csatorn??t " + emote).queue();
        } else {
            hook.editOriginal("Ezt a csatorn??t m??r figyelem " + emote).queue();
        }
    }

    private void onDeleteWatchedChannel(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event, hook);
        if(channelId == null) return;

        var optional = watchedChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        if(optional.isPresent()) {
            watchedChannelRepository.delete(optional.get());
            log.info("No longer watching channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            hook.editOriginal("Mostant??l NEM figyelem a <#" + channelId + "> csatorn??t.").queue();
        } else {
            hook.editOriginal("Ezt a csatorn??t nem is figyeltem.").queue();
        }
    }

    private void onListWatchedChannels(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        List<String> watchedChannels = getWatchedChannelNames(guildId);
        log.info("Listed the following watched channels on guild '{}': {}", event.getGuild().getName(), watchedChannels);

        if(watchedChannels.isEmpty()) {
            String emote = EmoteUtils.defaultEmote("sleeping");
            event.reply("Nem figyelek egyetlen csaton??t sem ezen a szerveren " + emote).queue();
        } else {
            String emote = EmoteUtils.defaultEmote("eyes");
            event.reply("Ezeket a csatorn??kat figyelem " + emote + " a szerveren: " + watchedChannels).queue();
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

    private void onAddAnnouncementChannel(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event, hook);
        if(channelId == null) return;

        var optional = announcementChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        String emote = EmoteUtils.defaultEmote("mega");
        if(optional.isEmpty()) {
            var announcementChannel = new AnnouncementChannel(event.getGuild().getIdLong(), channelId);
            announcementChannelRepository.save(announcementChannel);
            log.info("New announcement channel with id '{}' on guild '{}'", channelId, event.getGuild().getName());
            hook.editOriginal("A hirdet??seimet mostant??l <#" + channelId + "> csatorn??ra is kirakom " + emote).queue();
        } else {
            hook.editOriginal("Erre a csatorn??ra m??r most is ??rok hirdet??seket " + emote).queue();
        }
    }

    private void onDeleteAnnouncementChannel(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long channelId = getTargetChannelId(event, hook);
        if(channelId == null) return;

        var optional = announcementChannelRepository.getByGuildIdAndChannelId(guildId, channelId);
        if(optional.isPresent()) {
            announcementChannelRepository.delete(optional.get());
            log.info("Channel with id '{}' on guild '{}' is no longer an announcement channel.", channelId, event.getGuild().getName());
            hook.editOriginal("Mostant??l NEM haszn??lom hirdet??sekre <#" + channelId + "> csatorn??t.").queue();
        } else {
            hook.editOriginal("Ezt a csatorn??t nem is haszn??ltam hirdet??sre.").queue();
        }
    }

    private void onListAnnouncementChannels(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        List<String> announcementChannels = getAnnouncementChannelNames(guildId);
        log.info("Listed the following announcement channels on guild '{}': {}", event.getGuild().getName(), announcementChannels);
        if(announcementChannels.isEmpty()) {
            event.reply("Nem hirdetek egyetlen csaton??n sem ezen a szerveren.").queue();
        } else {
            String emote = EmoteUtils.defaultEmote("mega");
            event.reply("Ezeken a csatorn??kon hirdetek " + emote + " a szerveren: " + announcementChannels).queue();
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

}
