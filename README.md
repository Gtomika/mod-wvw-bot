## MoD WvW Bot

Discord bot to manage Guild Wars 2 WvW related stuff on a discord server.

## Slash commands

These commands use discords slash command system, so there is support in the client for them, such as 
name autocomplete.

**Permissions**: At first, only server admins can use these commands. Then, you can add more roles 
who can use the bot, with the ```/manager_role``` command (documentation below).

### /wvw_role

Used to mark a role on the server as "WvW-related". These roles will get mentions in announcements made 
by the bot. Discord client will offer autocomplete for actions and roles. Syntax:

```
/wvw_role [action] [role]
```

```[action]``` can be:
 - *add*: mark a role as WvW-related.
 - *delete*: un-mark a role.
 - *list*: List all WvW-related roles (in this case, don't specify ```[role]```). Anyone 
can invoke this command.

### /manager_role

Marks a role as manager of the bot. Users with these roles can also use the slash commands. The server 
admins can always use the commands regardless of these roles.

```
/manager_role [action] [role]
```

```[action]``` can be:
- *add*: mark a role as manager.
- *delete*: un-mark a role.
- *list*: List all manager roles (in this case, don't specify ```[role]```). Anyone
  can invoke this command.

### /watch_channel

Used to mark a channel where logs, EVTC files might be uploaded. Only on such "watched" channels will 
the bot pick these files up and start processing them.

```
/watch_channel [action] [text channel]
```

```[action]``` can be:
- *add*: mark channel as watched.
- *delete*: un-mark channel.
- *list*: List all watched channels (in this case, don't specify ```[text channel]```). Anyone
  can invoke this command.

### /announcement_channel

Used to mark a channel for announcement. The bot will create the configured announcements, 
reminders on these channels.

```
/announcement_channel [action] [text channel]
```

```[action]``` can be:
- *add*: mark channel as announcement target.
- *delete*: un-mark channel.
- *list*: List all announcement channels (in this case, don't specify ```[text channel]```). Anyone
  can invoke this command.

### /wvw_raid_add

Schedule a WvW raid at a given time. This uses a weekly cycle: if you schedule an event it will repeat every 
week at the given time. People who have WvW roles (```/wvw_role```) will get a reminder before the event on the 
announcement channels (```/announcement_channel```).

You can only schedule a wvw raid at xx:x0 or xx:x5 (in other words: 5 minutes is the 
smallest supported interval). Only one raid can start at a certain time.

```
/wvw_raid_add [time] [duration] [remind_time?]
```

```[time]``` is a string of day + time in the following format (examples):
- Saturday-18:00, Monday-20:35, Friday-12:20

Note the 5 minutes interval rule: xx:x0 or xx:x5, for example 18:31 is invalid.

```[duration]``` is the length of the event. It is a string in the following format (examples):
- 1h, 1h30m, 2h45m, 35m

If *m* is given it must be between 1 and 59. Minimum duration is 5 minutes, maximum is a day.

```[remind_time]``` is an optional parameter to control when will the bot remind the players about the 
event. Same format must be used as with ```[duration]``` (same minimum and maximum values apply).
- If not given, it'll default to 15 minutes.
- Use special value ```disable``` to disable reminder for this event.

**Example 1**: Schedule a raid at Monday 8 pm which lasts 1 and a half hour. Reminder 30 minutes before.

```
/wvw_raid_add Monday-20:00 1h30m 30m
```

**Example 2:** Schedule a raid at Sunday morning at 9:45, which lasts for 1 hour. No reminders.

```
/wvw_raid_add Sunday-09:45 1h disable
```

Please note that for now there is no way to edit an event. For that it must deleted and recreated. 

### /wvw_raid_delete

Delete a scheduled WvW raid. Raids are identified by the time they occur at.

```
/wvw_raid_delete [time]
```

```[time]``` must follow the same format as described at ```/wvw_raid_add```. 

**Example**: Delete the raid created at ```/wvw_raid_add``` example 2:

```
/wvw_raid_delete Sunday-9:45
```

### /wvw_raid_list

Get a list of scheduled wvw raids in the server, including start times, durations and reminders.

**Note:** everyone can invoke this command.

### /home_world

Set (or get) the home server of the guild. This is required to use commands 
that query the GW2 API for WvW status. Getting the home server does not 
require authentication, but setting it does.

```
/home_world [world_name?]
```

```[world_name]``` is optional, if it is not given then the command will return 
the currently set home world. Note that the name must be the full name of the world, 
including the language tag if there is any! For example:
 - *Piken Square*.
 - *Dzagonur [DE]*

**Additional functionality about home world**: The bot will query the GW2 API once a 
day to check the population of the home world. It will send a notification on the 
announcement channels if:
- The world was previously full, but now it isn't.
- The world previously had more space, but now it's full.

This can't be disabled, but it happens very rarely.

### Elite Insights parser

[Download](https://github.com/baaron4/GW2-Elite-Insights-Parser) and place elite insights parser into src/main/resources folder before attempting to 
run the bot. The parser is used by the bot, however, it isn't checked into git.

Credits to the creators of Elite Insight Parser.