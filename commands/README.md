## JSON-s for creating the bot commands

Details at https://discord.com/developers/docs/interactions/application-commands

### Creating commands

To create a global command, POST here:

```
https://discord.com/api/v8/applications/968767607501115462/commands
```

To create a server (guild) based command, POST here:

```
https://discord.com/api/v8/applications/968767607501115462/guilds/<guild_id>/commands"
```

Some guild IDs:
 - Test server: 968768762885046272
 - MoD server: ?

### Authorization

Attach header:

```
"Authorization": "Bot <bot_token>"
```