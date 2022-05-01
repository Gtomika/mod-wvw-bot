package com.gaspar.modwvwbot.services;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service which manages which users are able to invoke the bot commands.
 *  - Server admins are able to use the commands any time.
 *  - Users with {@link com.gaspar.modwvwbot.model.ManagerRole}s are able to use the commands.
 */
@Service
@Slf4j
public class AuthorizationService {

    private final RoleCommandsService roleCommandsService;

    //Lombok won't copy annotation, so this constructor is needed.
    //must use @Lazy, there is a circular dependency.
    @Autowired
    public AuthorizationService(@Lazy RoleCommandsService roleCommandsService) {
        this.roleCommandsService = roleCommandsService;
    }

    /**
     * Test if a member is authorized to invoke bot commands.
     */
    public boolean isUnauthorizedToManageBot(@Nullable Member member) {
        if(member == null) return true;
        if(member.getPermissions().contains(Permission.ADMINISTRATOR)) {
            return false; //admins can always manage
        }
        var managerRoleIds = roleCommandsService.getManagerRoleIds(member.getGuild().getIdLong());
        for(long managerRoleId: managerRoleIds) {
            if(member.getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList()).contains(managerRoleId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Authorizes a slash command. If unauthorized, a response is sent.
     * @return True if authorized. If false, then response was sent to the command already.
     */
    public boolean authorize(SlashCommandInteractionEvent event) {
        if(isUnauthorizedToManageBot(event.getMember())) {
            log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
            event.reply(getUnauthorizedMessage()).queue();
            return false;
        }
        return true;
    }

    public String getUnauthorizedMessage() {
        return "Hoppá, ehhez nincs jogosultságod! Nekem csak egy commander parancsolhat.";
    }
}
