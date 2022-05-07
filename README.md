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
reminders on these channels. It's recommended to have at least one of these set, as without 
it the bot won't communicate much of its reminders and messages. There is also 
no reason to set multiple, since it will just result in duplicate messages.

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

Raids must be specified in english, but when listed, they will be localised 
to hungarian, like *Péntek-20:00*.

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
day (at 16:00) to check the population of the home world. It will send a notification on the 
announcement channels if:
- The world was previously full, but now it isn't.
- The world previously had more space, but now it's full.

This can't be disabled, but it happens very rarely.

### /wvw_items

Fetches how many WvW related items and are present 
on your account. These are the items that are currently listed:

 - Memories of Battle
 - Emblems of the Conqueror
 - Emblems of the Avenger
 - Legendary Spikes
 - Gifts of Battle

This command can be called by anyone even in private messages, but it requires an API key. See the 
section below about API keys on how to add one.

### /wvw_currencies

Fetches how many WvW related currencies you have in your wallet. These 
currencies are listed:

 - Skirmish claim tickets.
 - Badges of honor
 - Proof of heroics (all kind)

This command can be called by anyone even in private messages, but it requires an API key. See the
section below about API keys on how to add one.

### /wvw_matchup

Gets the state of the current WvW matchup. This is based on the home 
world of the guild.

To make this command work, first make sure to set the home world of the 
guild using ```/home_world``` command (documentation above).

Anyone can call this command.

### /help

Shows general information about the bot and its documentation. Anyone 
can of course call this command.

### API keys

Certain commands access the protected parts of the Gw2 API, and so require 
an API key. Users can add their keys by sending a **private** message 
to the bot in the following format:

```
modwvwbot-apikey [API key here]
```

The bot will check and test the key, and respond accordingly. If the response 
is successful, the key is saved. It can later be updated with the 
same command.

**Note**: At least the following API key permissions are needed to make 
all commands work:
 - account
 - inventories
 - characters
 - wallet
 - progression

### Announcements using HTTP POST

The ```/announcement``` endpoint can be used to *POST* announcement requests to guilds. 

Specify the details in the request body:

```json
{
  "message": "Message to be announced",
  "guildIds": [
    1, 2, 3
  ]
}
```

The ```guildIds``` field is optional. If not specified, all announcement channels will be 
used to send the message. Otherwise, use discord guild IDs to limit the target guilds.

A security token is required for the endpoint to work:

```
[url]/announcement?token=mysecuritytoken
```

Token can be set using environmental variable ```SECURITY_TOKEN```.

Response is *JSON* with info about how many channels and guilds were affected.

### Bot variations

There are 2 discord bots that run the code:
 - *Mod WvW Bot*: Live version of the bot which supports any sever with global commands.
 - *Mod WvW Bot \[DEV]*: The development version which supports only the test server 
with server specific commands.

### Elite Insights parser

**NOT USED FOR NOW**. Don't have to do this!

[Download](https://github.com/baaron4/GW2-Elite-Insights-Parser) and place elite insights parser into src/main/resources folder before attempting to 
run the bot. The parser is used by the bot, however, it isn't checked into git.

Credits to the creators of Elite Insight Parser.