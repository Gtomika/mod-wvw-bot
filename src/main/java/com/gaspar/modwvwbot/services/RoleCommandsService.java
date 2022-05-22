package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.SlashCommandHandler;
import com.gaspar.modwvwbot.model.ManagerRole;
import com.gaspar.modwvwbot.model.WvwRole;
import com.gaspar.modwvwbot.repository.ManagerRoleRepository;
import com.gaspar.modwvwbot.repository.WvwRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
public class RoleCommandsService implements SlashCommandHandler {

    private static final String WVW_ROLE_COMMAND = "/wvw_role";

    private static final String MANAGER_ROLE_COMMAND = "/manager_role";

    private static final String OPTION_ACTION = "action";

    private static final String OPTION_ROLE = "role";

    private final WvwRoleRepository wvwRoleRepository;
    private final ManagerRoleRepository managerRoleRepository;
    private final AuthorizationService authorizationService;

    @Override
    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        var optionAction = getOptionAction(event);
        if(optionAction == null) return;
        if(event.getCommandString().startsWith(WVW_ROLE_COMMAND)) {
            switch (optionAction.getAsString()) {
                case "wvw_role_add":
                    event.deferReply().setEphemeral(true).queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onAddWvwRole(event, hook);
                        }
                    });
                    break;
                case "wvw_role_delete":
                    event.deferReply().setEphemeral(true).queue(hook -> {
                        if(authorizationService.authorize(event, hook)) {
                            onDeleteWvwRole(event, hook);
                        }
                    });
                    break;
                case "wvw_role_list":
                    onListWvwRole(event);
                    break;
                default:
                    log.info("Unknown value for option 'action': {}", optionAction.getAsString());
                    event.reply("Nem megengedett érték: csak 'add', 'delete' vagy list lehet.").queue();
            }
        } else if(event.getCommandString().startsWith(MANAGER_ROLE_COMMAND)) {
           switch (optionAction.getAsString()) {
               case "manager_role_add":
                   event.deferReply().setEphemeral(true).queue(hook -> {
                       if(authorizationService.authorize(event, hook)) {
                           onAddManagerRole(event, hook);
                       }
                   });
                   break;
               case "manager_role_delete":
                   event.deferReply().setEphemeral(true).queue(hook -> {
                       if(authorizationService.authorize(event, hook)) {
                           onDeleteManagerRole(event, hook);
                       }
                   });
                   break;
               case "manager_role_list":
                   onListManagerRole(event);
                   break;
               default:
                   log.info("Unknown value for option 'action': {}", optionAction.getAsString());
                   event.reply("Nem megengedett érték: csak 'add', 'delete' vagy list lehet.").queue();
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
        return new String[] {WVW_ROLE_COMMAND, MANAGER_ROLE_COMMAND};
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

    private void onAddWvwRole(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = wvwRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isEmpty()) {
            var role = new WvwRole(guildId, roleId);
            wvwRoleRepository.save(role);
            log.info("Role with id '{}' is now a WvW rank in guild '{}'", roleId, event.getGuild().getName());
            hook.editOriginal("A <@&" + roleId + "> mostantól egy WvW rang.").queue();
        } else {
            hook.editOriginal("A <@&" + roleId + "> már most is egy WvW rang.").queue();
        }
    }

    private void onDeleteWvwRole(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = wvwRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isPresent()) {
            wvwRoleRepository.delete(optional.get());
            log.info("Role with id '{}' is no longer a WvW rank in guild '{}'", roleId, event.getGuild().getName());
            hook.editOriginal("A <@&" + roleId + "> mostantól nem WvW rang.").queue();
        } else {
            hook.editOriginal("A <@&" + roleId + "> nem is volt WvW rang.").queue();
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

    private void onAddManagerRole(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = managerRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isEmpty()) {
            var role = new ManagerRole(guildId, roleId);
            managerRoleRepository.save(role);
            log.info("Role with id '{}' is now a manager rank in guild '{}'", roleId, event.getGuild().getName());
            hook.editOriginal("A <@&" + roleId + "> mostantól egy kezelő rang.").queue();
        } else {
            hook.editOriginal("A <@&" + roleId + "> már egy kezelő rang.").queue();
        }
    }

    private void onDeleteManagerRole(SlashCommandInteractionEvent event, InteractionHook hook) {
        long guildId = event.getGuild().getIdLong();
        Long roleId = getTargetRoleId(event);
        if(roleId == null) return;

        var optional = managerRoleRepository.findByGuildIdAndRoleId(guildId, roleId);
        if(optional.isPresent()) {
            managerRoleRepository.delete(optional.get());
            log.info("Role with id '{}' is no longer a manager rank in guild '{}'", roleId, event.getGuild().getName());
            hook.editOriginal("A <@&" + roleId + "> rang mostantól nem kezelő rang.").queue();
        } else {
            hook.editOriginal("A <@&" + roleId + "> nem is volt kezelő rang.").queue();
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
