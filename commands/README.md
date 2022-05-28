## JSON-s for creating the bot commands

Details at [application commands documentation](https://discord.com/developers/docs/interactions/application-commands)

### Bot variants

The bot has a *live* and a *development* version:
 - The *live* version uses global commands, these are in the *live* folder. Bot ID: ```972523867270705172```
 - The *dev* version only has commands on the test server, these are located in the *dev* folder. Bot ID: ```968767607501115462```

### Creating commands

To create a global command for the *live* Bot, POST here:

```
https://discord.com/api/v8/applications/972523867270705172/commands
```

To create a server (guild) based command for the *dev* Bot, POST here:

```
https://discord.com/api/v8/applications/968767607501115462/guilds/<guild_id>/commands"
```

Some guild IDs:
 - Test server: 968768762885046272
 - Staging Server: 973656775662399519
 - MoD server: 290549656276959242

### Authorization of Discord endpoints

Attach header:

```
"Authorization": "Bot <bot_token>"
```