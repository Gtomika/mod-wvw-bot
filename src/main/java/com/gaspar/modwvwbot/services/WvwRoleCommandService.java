package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.model.WvwRole;
import com.gaspar.modwvwbot.repository.WvwRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Watches for events from /wvw_role command.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WvwRoleCommandService extends ListenerAdapter {

    private static final String WVW_ROLE_COMMAND = "/wvw_role";

    private static final String OPTION_ACTION = "action";

    private static final String OPTION_ROLE = "role";

    private final WvwRoleRepository wvwRoleRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WVW_ROLE_COMMAND)) {

            if(event.getGuild() == null) {
                log.error("Slash command '/wvw_role' must be sent from a guild.");
                event.reply("Ezt a parancsot csak szerverről lehet küldeni.").queue();
                return;
            }

            log.info("Received /wvw_role command from user '{}' in guild {}", event.getUser().getName(), event.getGuild());

            var optionAction = event.getOption(OPTION_ACTION);
            if(optionAction == null) {
                log.warn("Required option 'action' was null when processing '/wvw_role' command.");
                event.reply("Hiba: az 'action' értéket meg kell adni.").queue();
                return;
            }

            switch (optionAction.getAsString()) {
                case "wvw_role_add":
                    onAddWvwRole(event);
                    break;
                case "wvw_role_delete":
                    onDeleteWvwRole(event);
                    break;
                case "wvw_role_list":
                    onListWvwRole(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy list lehet.").queue();
            }
        }
    }

    /**
     * Extract the target role of the event. This is the one that is to be added/deleted.
     */
    @Nullable
    private Long getTargetRoleId(SlashCommandInteractionEvent event) {
        var optionChannel = event.getOption(OPTION_ROLE);
        if(optionChannel == null) {
            event.reply("Hiba: a 'role' értéknek meg kell adni egy rangot.").queue();
            return null;
        }
        try {
            Role role = optionChannel.getAsRole();
            return role.getIdLong();
        } catch (IllegalStateException e) {
            event.reply("Hiba: a 'role' értéknek egy rangot kell adni.").queue();
            return null;
        }
    }

    private void onAddWvwRole(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = wvwRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isEmpty()) {
            var role = new WvwRole(guildId, roleId);
            wvwRoleRepository.save(role);
            log.info("Role with id '{}' is now a WvW rank in guild '{}'", roleId, event.getGuild().getName());
            event.reply("A <@&" + roleId + "> mostantól egy WvW rang.").setEphemeral(true).queue();
        } else {
            event.reply("Ez már most is egy WvW rang.").queue();
        }
    }

    private void onDeleteWvwRole(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = wvwRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isPresent()) {
            wvwRoleRepository.delete(optional.get());
            log.info("Role with id '{}' is no longer a WvW rank in guild '{}'", roleId, event.getGuild().getName());
            event.reply("A <@&" + roleId + "> rang mostantól nem WvW rang.").setEphemeral(true).queue();
        } else {
            event.reply("Ez a rang nem is volt WvW rang.").queue();
        }
    }

    private void onListWvwRole(SlashCommandInteractionEvent event) {
        var formattedRoles = wvwRoleRepository.findByGuildId(event.getGuild().getIdLong())
                .stream()
                .map(role -> "<@&" + role.getRoleId() + ">")
                .collect(Collectors.toList());
        log.info("Listed the following (formatted) role IDs as WvW roles in guild '{}': {}", event.getGuild().getName(), formattedRoles);
        if(!formattedRoles.isEmpty()) {
            event.reply("Ezek a szerver WvW rangjai: " + formattedRoles).setEphemeral(true).queue();
        } else {
            event.reply("Még nincs egy WvW rang sem hozzáadva a szerveren.").queue();
        }
    }
}
