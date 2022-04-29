## MoD WvW Bot

Discord bot to manage Guild Wars 2 WvW related stuff on a discord server.

## Slash commands

These commands use discords slash command system, so there is support in the client for them, such as 
name autocomplete.

**Permissions**: At first, only server admins can use these commands. Then, you can add more roles 
who can use the bot, with the ```/manager_role``` command (documentation below).

#### /wvw_role

Used to mark a role on the server as "WvW-related". These roles will get mentions in announcements made 
by the bot. Discord client will offer autocomplete for actions and roles. Syntax:

```
/wvw_role [action] [role]
```

```[action]``` can be:
 - *add*: mark a role as WvW-related.
 - *delete*: un-mark a role.
 - *list*: List all WvW-related roles (in this case, don't specify ```[role]```).

#### /manager_role

Marks a role as manager of the bot. Users with these roles can also use the slash commands. The server 
admins can always use the commands regardless of these roles.

```
/manager_role [action] [role]
```

```[action]``` can be:
- *add*: mark a role as manager.
- *delete*: un-mark a role.
- *list*: List all manager roles (in this case, don't specify ```[role]```).

#### /watch_channel

Used to mark a channel where logs, EVTC files might be uploaded. Only on such "watched" channels will 
the bot pick these files up and start processing them.

```
/watch_channel [action] [text channel]
```

```[action]``` can be:
- *add*: mark channel as watched.
- *delete*: un-mark channel.
- *list*: List all watched channels (in this case, don't specify ```[text channel]```).

#### /announcement_channel

Used to mark a channel for announcement. The bot will create the configured announcements, 
reminders on these channels.

```
/announcement_channel [action] [text channel]
```

```[action]``` can be:
- *add*: mark channel as announcement target.
- *delete*: un-mark channel.
- *list*: List all announcement channels (in this case, don't specify ```[text channel]```).

### Elite Insights parser

[Download](https://github.com/baaron4/GW2-Elite-Insights-Parser) and place elite insights parser into src/main/resources folder before attempting to 
run the bot. The parser is used by the bot, bot it isn't checked into git.