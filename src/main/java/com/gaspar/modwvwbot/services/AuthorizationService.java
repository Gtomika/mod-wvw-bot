package com.gaspar.modwvwbot.services;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
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
public class AuthorizationService {

    private RoleCommandsService roleCommandsService;

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

    public String getUnauthorizedMessage() {
        return "Nekem te nem parancsolhatsz!";
    }
}
