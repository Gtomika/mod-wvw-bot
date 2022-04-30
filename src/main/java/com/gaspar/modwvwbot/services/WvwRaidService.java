package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.misc.TimeUtils;
import com.gaspar.modwvwbot.model.WvwRaid;
import com.gaspar.modwvwbot.repository.WvwRaidRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Handles wvw raid slash commands.
 */
@Service
@Slf4j
public class WvwRaidService extends ListenerAdapter {

    private static final String WVW_RAID_ADD_COMMAND = "/wvw_raid_add";

    private static final String WVW_RAID_DELETE_COMMAND = "/wvw_raid_delete";

    private static final String WVW_RAID_LIST_COMMAND = "/wvw_raid_list";

    private static final String OPTION_TIME = "time";

    private static final String OPTION_DURATION = "duration";

    private static final String OPTION_REMIND_TIME = "remind_time";

    @Value("${com.gaspar.modwvwbot.default_reminder_minutes}")
    private int defaultReminderMinutes;

    private final AuthorizationService authorizationService;
    private final WvwRaidRepository wvwRaidRepository;
    private final ChannelCommandsService channelCommandsService;
    private final RoleCommandsService roleCommandsService;
    private final JDA jda;

    //lombok won't copy lazy annotation so this must be here
    public WvwRaidService(AuthorizationService authorizationService, WvwRaidRepository wvwRaidRepository,
                          ChannelCommandsService channelCommandsService, RoleCommandsService roleCommandsService,
                          @Lazy JDA jda) {
        this.authorizationService = authorizationService;
        this.wvwRaidRepository = wvwRaidRepository;
        this.channelCommandsService = channelCommandsService;
        this.roleCommandsService = roleCommandsService;
        this.jda = jda;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WVW_RAID_ADD_COMMAND)) {
            //auth
            if(authorizationService.isUnauthorizedToManageBot(event.getMember())) {
                log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
                event.reply(authorizationService.getUnauthorizedMessage()).queue();
                return;
            }
            onWvwRaidAddCommand(event);
        } else if(event.getCommandString().startsWith(WVW_RAID_DELETE_COMMAND)) {
            //auth
            if(authorizationService.isUnauthorizedToManageBot(event.getMember())) {
                log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
                event.reply(authorizationService.getUnauthorizedMessage()).queue();
                return;
            }
            onWvwRaidDeleteCommand(event);
        } else if(event.getCommandString().startsWith(WVW_RAID_LIST_COMMAND)) {
            //not authorized command
            onWvwRaidListCommand(event);
        }
    }

    private void onWvwRaidAddCommand(SlashCommandInteractionEvent event) {
        String time = getValidStandardizedOptionTime(event);
        if(time == null) return;
        Integer minutes = getValidDurationMinutes(event);
        if(minutes == null) return;
        //can be null
        Integer remindMinutes = getValidReminderOptionMinutes(event);
        String remindTime = null;
        if(remindMinutes != null) {
            remindTime = TimeUtils.createReminderTimeStringRoundedToFiveMinutes(time, remindMinutes);
        }

        WvwRaid raid = WvwRaid.builder()
                .guildId(event.getGuild().getIdLong())
                .time(time)
                .durationMinutes(minutes)
                .remindTimeMinutes(remindMinutes)
                .remindTime(remindTime)
                .build();

        //is there a raid already at that time?
        var optional = wvwRaidRepository.findByGuildIdAndTime(event.getGuild().getIdLong(), time);
        if(optional.isEmpty()) {
            wvwRaidRepository.save(raid);
            log.info("Wvw raid added for guild '{}' by '{}': {}", event.getGuild().getName(), event.getUser().getName(), raid);
            event.reply("Elmentettem a WvW raidet " + time + " időpontban.").queue();
        } else {
            log.debug("Raid at time '{}' is already present in guild '{}'", time, event.getGuild().getName());
            event.reply("A " + time + " időpontban már van egy WvW raid.").queue();
        }
    }

    /**
     * Get {@link #OPTION_TIME} command parameter. Validates and standardizes it.
     * @return The time, or null if it could not be found or was not valid.
     */
    @Nullable
    private String getValidStandardizedOptionTime(SlashCommandInteractionEvent event) {
        OptionMapping optionTime = event.getOption(OPTION_TIME);
        if(optionTime == null) {
            log.debug("Unable to extract 'time' parameter from command '{}'. This command is invalid.", event.getCommandString());
            event.reply("Hiba: A 'time' paramétert meg kell adni.").queue();
            return null;
        }
        if(!TimeUtils.isValidTimeString(optionTime.getAsString())) {
            log.debug("Invalid time string '{}', the command '{}' is invalid.", optionTime.getAsString(), event.getCommandString());
            event.reply("Hiba: Érvénytelen időformátum a 'time' paraméternél.").queue();
            return null;
        }
        return TimeUtils.standardizeTimeString(optionTime.getAsString());
    }

    /**
     * Gets the valid parameter 'duration' from the command, converted to minutes.
     * @return The duration in minutes, or null if it was not found/invalid.
     */
    @Nullable
    private Integer getValidDurationMinutes(SlashCommandInteractionEvent event) {
        OptionMapping optionDuration = event.getOption(OPTION_DURATION);
        if(optionDuration == null) {
            log.debug("Unable to extract 'duration' parameter from command '{}'. This command is invalid.", event.getCommandString());
            event.reply("Hiba: A 'duration' paramétert meg kell adni, pl: 1h45m, 30m..").queue();
            return null;
        }
        int minutes = TimeUtils.parseDurationToMinutes(optionDuration.getAsString());
        if(minutes == 0) {
            log.debug("Incorrect 'duration' parameter '{}' in command '{}'.", optionDuration.getAsString(), event.getCommandString());
            event.reply("Hiba: A 'duration' paraméternek egy időtartamot kell megadni, pl: 1h45m, 30m.").queue();
            return null;
        }
        if(minutes < 5 || minutes > TimeUtils.MINUTES_IN_DAY) {
            log.debug("Too much or little time in 'duration' parameter '{}' in command '{}'.",
                    optionDuration.getAsString(), event.getCommandString());
            event.reply("Hiba: A 'duration' időtartam minimum 5 perc, maximum egy nap.").queue();
            return null;
        }
        return minutes;
    }

    /**
     * Gets valid reminder time in minutes from the command. This is an optional parameter.
     * @return The time in minutes or null if it was not specified.
     */
    @Nullable
    private Integer getValidReminderOptionMinutes(SlashCommandInteractionEvent event) {
        OptionMapping optionReminder = event.getOption(OPTION_REMIND_TIME);
        if(optionReminder == null) {
            return null; //not required
        }
        int minutes = TimeUtils.parseDurationToMinutes(optionReminder.getAsString());
        if(minutes == 0) {
            log.debug("Incorrect 'remind_time' parameter '{}' in command '{}'.", optionReminder.getAsString(), event.getCommandString());
            event.reply("Hiba: A 'remind_time' paraméternek egy időtartamot kell megadni, pl: 1h vagy 15m.").queue();
            return null;
        }
        if(minutes < 5 || minutes > TimeUtils.MINUTES_IN_DAY) {
            log.debug("Too much or little time in 'remind_time' parameter '{}' in command '{}'.",
                    optionReminder.getAsString(), event.getCommandString());
            event.reply("Hiba: A 'remind_time' időtartam minimum 5 perc, maximum egy nap.").queue();
            return null;
        }
        return minutes;
    }

    private void onWvwRaidDeleteCommand(SlashCommandInteractionEvent event) {
        String time = getValidStandardizedOptionTime(event);
        if(time == null) return;
        var optional = wvwRaidRepository.findByGuildIdAndTime(event.getGuild().getIdLong(), time);
        if(optional.isPresent()) {
            wvwRaidRepository.delete(optional.get());
            log.info("Deleted WvW raid at time '{}' in guild '{}'.", time, event.getGuild().getName());
            event.reply("Mostantól nincs WvW raid a " + time + " időpontban.").queue();
        } else {
            log.debug("No WvW raid at time '{}' in guild '{}', not deleting anything.", time, event.getGuild().getName());
            event.reply("A " + time + " időpontban nincs is WvW raid.").queue();
        }
    }

    private void onWvwRaidListCommand(SlashCommandInteractionEvent event) {
        var raidsAsStrings = wvwRaidRepository.findByGuildId(event.getGuild().getIdLong())
                .stream()
                .map(raid -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Időpont: ").append(raid.getTime()).append(", ");
                    String durationString = TimeUtils.createDurationStringFromMinutes(raid.getDurationMinutes());
                    builder.append("Hossz: ").append(durationString).append(", ");
                    String reminderString = raid.getRemindTimeMinutes() == null ? TimeUtils.createDurationStringFromMinutes(defaultReminderMinutes)
                            : TimeUtils.createDurationStringFromMinutes(raid.getRemindTimeMinutes());
                    builder.append("Emlékeztető ennyivel előtte: ").append(reminderString);
                    return builder.toString();
                })
                .collect(Collectors.toList());
        log.info("Listed {} WvW raids in guild '{}'", raidsAsStrings.size(), event.getGuild().getName());
        StringBuilder builder = new StringBuilder();
        builder.append("Ezekről a WvW raidekről tudok:\n");
        for(String raidString: raidsAsStrings) {
            builder.append(" - ").append(raidString).append("\n");
        }
        event.reply(builder.toString()).queue();
    }

    /**
     * Runs a job every 5 minutes to see if reminder need to be posted about a WvW raid.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    private void runWvwRaidJob() {
        String currentRoundedTime = TimeUtils.getCurrentTimeStringRoundedFiveMinutes();
        log.debug("Running WvW raid job at {}", currentRoundedTime);
        //get all raids with reminders at this time
        //expected at most 1 for each guild
        var raids = wvwRaidRepository.findByRemindTime(currentRoundedTime);
        for(WvwRaid raid: raids) {
            Guild guild = jda.getGuildById(raid.getGuildId());
            if(guild == null) {
                log.info("Can't find guild with ID '{}' when trying to send reminder. Maybe it was deleted.", raid.getGuildId());
                continue;
            }
            log.info("Attempting to send reminder to guild '{}' about this raid: {}", guild.getName(), raid);
            //get announcement channels for the guild where this event is going to happen
            var announcementChannels = channelCommandsService.getAnnouncementChannels(raid.getGuildId());
            if(announcementChannels.isEmpty()) {
                log.info("Wvw raid reminder should be posted in guild with ID '{}', but there are no announcement channels.", raid.getGuildId());
                continue;
            }
            //get WvW roles who need a mention in the guild of the event
            var wvwRoles = roleCommandsService.getWvwRoleIdsFormatted(raid.getGuildId());
            if(wvwRoles.isEmpty()) {
                log.info("Wvw raid reminder will be posted in guild with ID '{}' but there are no WvW roles set. Nobody will get pinged.", raid.getGuildId());
            }
            //compose message
            final StringBuilder message = new StringBuilder();
            message.append("WvW raid kezdődik ").append(raid.getRemindTimeMinutes()).append(" perc múlva!\n");
            String duration = TimeUtils.createDurationStringFromMinutes(raid.getDurationMinutes());
            message.append("Időpont: ").append(raid.getTime()).append(", Hossz: ").append(duration).append("\n");
            for(int i = 0; i<wvwRoles.size(); i++) {
                message.append(wvwRoles.get(i));
                if(i < wvwRoles.size()-1) {
                    message.append(", ");
                }
            }
            //post the message on every announcement channel
            for(var announcementChannelId: announcementChannels) {
                TextChannel channel = jda.getTextChannelById(announcementChannelId);
                if(channel == null) {
                    log.info("Announcement channel with ID '{}' not found when trying to post reminder. Maybe it was deleted.", announcementChannelId);
                    continue;
                }
                if(!channel.canTalk()) {
                    log.info("Bot cannot talk on announcement channel with ID '{}'. Skipping.", announcementChannelId);
                    continue;
                }
                channel.sendMessage(message.toString()).queue();
            }
        }
    }
}
