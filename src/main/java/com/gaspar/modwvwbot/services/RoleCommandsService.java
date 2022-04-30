package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.model.ManagerRole;
import com.gaspar.modwvwbot.model.WvwRole;
import com.gaspar.modwvwbot.repository.ManagerRoleRepository;
import com.gaspar.modwvwbot.repository.WvwRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Watches for events from /wvw_role command.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleCommandsService extends ListenerAdapter {

    private static final String WVW_ROLE_COMMAND = "/wvw_role";

    private static final String MANAGER_ROLE_COMMAND = "/manager_role";

    private static final String OPTION_ACTION = "action";

    private static final String OPTION_ROLE = "role";

    private final WvwRoleRepository wvwRoleRepository;
    private final ManagerRoleRepository managerRoleRepository;
    private final AuthorizationService authorizationService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getCommandString().startsWith(WVW_ROLE_COMMAND) || event.getCommandString().startsWith(MANAGER_ROLE_COMMAND)) {
            //authorization
            if(authorizationService.isUnauthorizedToManageBot(event.getMember())) {
                log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
                event.reply(authorizationService.getUnauthorizedMessage()).queue();
                return;
            }

            var optionAction = getOptionAction(event);
            if(optionAction == null) return;

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
                case "manager_role_add":
                    onAddManagerRole(event);
                    break;
                case "manager_role_delete":
                    onDeleteManagerRole(event);
                    break;
                case "manager_role_list":
                    onListManagerRole(event);
                    break;
                default:
                    log.error("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy list lehet.").queue();
            }
        }
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

        log.info("Received '{}' command from user '{}' in guild {}", event.getCommandString(), event.getUser().getName(), event.getGuild());

        var optionAction = event.getOption(OPTION_ACTION);
        if(optionAction == null) {
            log.warn("Required option 'action' was null when processing '{}' command.", event.getCommandString());
            event.reply("Hiba: az 'action' értéket meg kell adni.").queue();
            return null;
        }
        return optionAction;
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
        var formattedRoles = getWvwRoleIdsFormatted(event.getGuild().getIdLong());
        log.info("Listed the following (formatted) role IDs as WvW roles in guild '{}': {}", event.getGuild().getName(), formattedRoles);
        if(!formattedRoles.isEmpty()) {
            event.reply("Ezek a szerver WvW rangjai: " + formattedRoles).setEphemeral(true).queue();
        } else {
            event.reply("Még nincs egy WvW rang sem hozzáadva a szerveren.").queue();
        }
    }

    private void onAddManagerRole(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = managerRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isEmpty()) {
            var role = new ManagerRole(guildId, roleId);
            managerRoleRepository.save(role);
            log.info("Role with id '{}' is now a manager rank in guild '{}'", roleId, event.getGuild().getName());
            event.reply("A <@&" + roleId + "> mostantól egy kezelő rang.").setEphemeral(true).queue();
        } else {
            event.reply("Ez már most is egy kezelő rang.").queue();
        }
    }

    private void onDeleteManagerRole(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = managerRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isPresent()) {
            managerRoleRepository.delete(optional.get());
            log.info("Role with id '{}' is no longer a manager rank in guild '{}'", roleId, event.getGuild().getName());
            event.reply("A <@&" + roleId + "> rang mostantól nem kezelő rang.").setEphemeral(true).queue();
        } else {
            event.reply("Ez a rang nem is volt kezelő rang.").queue();
        }
    }

    private void onListManagerRole(SlashCommandInteractionEvent event) {
        var formattedRoles = managerRoleRepository.findByGuildId(event.getGuild().getIdLong())
                .stream()
                .map(role -> "<@&" + role.getRoleId() + ">")
                .collect(Collectors.toList());
        log.info("Listed the following (formatted) role IDs as manager roles in guild '{}': {}", event.getGuild().getName(), formattedRoles);
        if(!formattedRoles.isEmpty()) {
            event.reply("Ezek a bot kezelő rangjai: " + formattedRoles + ". Továbbá, az adminok is tudnak parancsolni nekem.").setEphemeral(true).queue();
        } else {
            event.reply("Még nincs egy kezelő rang sem hozzáadva a szerveren. Csak adminok tudnak parancsolni nekem.").queue();
        }
    }

    /**
     * Get list of manager roles in a guild.
     */
    public List<Long> getManagerRoleIds(long guildId) {
        return managerRoleRepository.findByGuildId(guildId)
                .stream()
                .map(ManagerRole::getRoleId)
                .collect(Collectors.toList());
    }

    public List<String> getWvwRoleIdsFormatted(long guildId) {
        return wvwRoleRepository.findByGuildId(guildId)
                .stream()
                .map(role -> "<@&" + role.getRoleId() + ">")
                .collect(Collectors.toList());
    }

    public List<Long> getWvwRoleIds(long guildId) {
        return wvwRoleRepository.findByGuildId(guildId)
                .stream()
                .map(WvwRole::getRoleId)
                .collect(Collectors.toList());
    }
}
