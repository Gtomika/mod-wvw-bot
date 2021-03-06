## MoD WvW Bot

Discord bot to manage Guild Wars 2 WvW related stuff on a discord server.

Invite link:

```
https://discord.com/api/oauth2/authorize?client_id=972523867270705172&permissions=277025704000&scope=bot%20applications.commands
```

## Slash commands

These commands use discords slash command system, so there is support in the client for them, such as 
name autocomplete.

**Permissions**: At first, only server admins can use these commands. Then, you can add more roles 
who can use the bot, with the ```/manager_role``` command (documentation below).

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
- unlocks

### /wvw_items

Fetches how many WvW related items and are present
on your account. These are the items that are currently listed:

- Memories of Battle
- Emblems of the Conqueror
- Emblems of the Avenger
- Legendary Spikes
- Gifts of Battle

This command can be called by anyone even in private messages, but it requires an API key. See the
section about API keys on how to add one.

![screenshot](/images/screenshot_wvw_items.png)

### /wvw_currencies

Fetches how many WvW related currencies you have in your wallet. These
currencies are listed:

- Skirmish claim tickets.
- Badges of honor
- Proof of heroics (all kind)

This command can be called by anyone even in private messages, but it requires an API key. See the
section about API keys on how to add one.

![screenshot](/images/screenshot_wvw_currencies.png)

### /wvw_matchup

Gets the state of the current WvW matchup. This is based on the home
world of the guild. The bot will try to predict **based on current placements** 
which world will be matched up against the guilds home world. If the 
placements change until reset, this prediction will of course be inaccurate.

This command will not make predictions if the next week is relink. In this case no 
predictions can be made at all.

To make this command work, first make sure to set the home world of the
guild using ```/home_world``` command. Anyone can call this command.

![screenshot](/images/screenshot_wvw_matchup.png)

### /next_wvw_matchup

Displays a prediction based on current standings: what tier will the home server 
of the guild be in, and what opponents will they face.

To make this command work, first make sure to set the home world of the
guild using ```/home_world``` command. Anyone can call this command.

![screenshot](/images/screenshot_prediction.png)

### /wvw_legendaries

Displays the Wvw related legendary items on your account. Can be used by anyone, even in private
messages. Requires an API key. See the section about API keys on how to add one.

Due to technical difficulties, the legendary Wvw armor is not listed, only these items are:

- Conflux (ring)
- Warbringer (backpack)

![screenshot](/images/screenshot_wvw_legendaries.png)

### /wvw_rank

The bot will display your WvW rank and some additional information along with it. Can be used by anyone, even in private
messages. Requires an API key. See the section about API keys on how to add one.

![screenshot](/images/screenshot_wvw_rank.png)

### /help

Shows general information about the bot and its documentation. Anyone can of course call this command.

![screenshot](/images/screenshot_help.png)

### /wvw_role

Used to mark a role on the server as "WvW-related". These roles will get mentions in announcements made 
by the bot. Syntax:

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
to hungarian, like *P??ntek-20:00*.

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

![screenshot](/images/screenshot_wvw_raids.png)

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

![screenshot](/images/screenshot_home_world.png)

## API

Some interactions are available with the bot's API. Security token is required for these to work, 
which can be set with the ```SECURITY_TOKEN``` environmental variable. The token is to be specified as 
a request parameter: ```[url]?token=[token]```

### Announcements API (POST)

The ```api/announcement``` endpoint can be used to *POST* announcement requests to guilds. 
Specify the details in the request body:

```
{
  "message": [string],
  "guildIds": [long array],
  "mentionWvwRoles": [boolean],
  "mentionManagerRoles": [boolean]
}
```

The ```guildIds``` field is optional. If not specified, all announcement channels will be 
used to send the message. Otherwise, use discord guild IDs to limit the target guilds.

Response is *JSON* with info about how many channels and guilds were affected.

### Statistics API (GET)

The ```/api/stats/guild/{guildId}``` endpoint returns data about bot command usage in a guild. Specify 
0 as guild ID to get statistics about commands in private messages.

```
{
  "usage": [
    {
      "year": [integer],
      "month": [month enum],
      "commands": [
        {
          "name": [string],
          "count": [integer]
        },
        ... //more commands
      ]
    },
    ... //more year+month data
  ] 
}
```

The ```/api/stats/command/{commandName}``` endpoint returns data about usage of a single command
(across all guilds). The command name in the URL must not include the slash at the start of the command. 
For example use when you want the ```/wvw_items``` command, use ```wvw_items```.

```
{
  "usage": [
    {
      "year": [integer],
      "month": [month enum],
      "count": [integer]
    },
    ... //more year+month data
  ]
}
```

### Guilds API (GET)

The ```/api/guilds``` endpoint returns the guilds with names and IDs that the bot is in.

```
{
  "guilds": [
    {
      "guildName": [string],
      "guildId": [long]
    },
    ... //more guilds
  ]
}
```

### Bot variations

There are 2 discord bots that run the code:
 - *Mod WvW Bot*: Live version of the bot which supports any sever with global commands.
 - *Mod WvW Bot \[DEV]*: The development version which supports only the test server 
with server specific commands.

Development bot invite link:

```
https://discord.com/api/oauth2/authorize?client_id=968767607501115462&permissions=277025704000&scope=bot%20applications.commands
```

### Build

With Maven:

```
mvn clean package -DskipTests=true
```