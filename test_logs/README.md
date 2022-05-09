### Parsed JSON logs of a WvW encounter

These logs are massive. Need to filter fields and only keep the ones that are later used in 
data visualization.

### Fields used in Power Bi file

This sample JSON only contains the fields used in power bi.

```
{
  KEEP ALL TOP LEVEL FIELDS
  ...
  "players": [
    {
         "account": "somebody.1234", //TOP LEVEL player info (keep all, it's not much)
         "group": 2,
         "hasCommanderTag": false,
         "profession": "Scourge",
         "friendlyNPC": false,
         "notInSquad": false,
         "guildID": "FBA78570-4184-4FB2-9091-BB6D52676B10",
         "weapons": [
           "Scepter",
           "Torch"
         ]
        "extHealingStats": { //detailed healing the player did
            "outgoingHealing": [
              [
               {
                 "hps": 0,
                  "healing": 0,
                  "healingPowerHps": 0,
                  "healingPowerHealing": 0,
                  "conversionHps": 0,
                  "conversionHealing": 0,
                  "hybridHps": 0,
                  "hybridHealing": 0,
                  "downedHps": 0,
                  "downedHealing": 0,
                  ...
               }
              ],
              ...
            ],
            "outgoingHealingAllies": [
              [
                {
                  //same fields as 'outgoingHealing'
                }
              ],
              ...
            ] 
        },
        "buffUptimes": [ //boons the players had
            {
              "id": 2142
              "buffData": [
                  {
                     "uptime": 100.0
                     ...
                  }
                  //other appearances of this boon in this player
              ]
            },
            ... //other boons
        ],
        "defenses": [ //defensive actions of the player
          {
              "damageTaken": 241121,
              "breakbarDamageTaken": 0.0,
              "blockedCount": 1,
              "evadedCount": 9,
              "missedCount": 0,
              "dodgeCount": 11,
              "invulnedCount": 48,
              "damageBarrier": 9715,
              "interruptedCount": 5,
              "downCount": 5,
              "downDuration": 27605,
              "deadCount": 3,
              "deadDuration": 17396,
              "dcCount": 0,
              "dcDuration": 0,
              ...
          },
          ...
        ],
        "dpsAll": [ //DPS summary stats of the player
          {
              "dps": 1000,
              "damage": 100000,
              "breakbarDamage": 4,
              ...
          },
          ... //more DPS stats
        ],
        "dpsTargets": [[ //detailed DPS separated by targets of a player (yes, a double nested array)
          {
              "dps": 1000,
              "damage": 10000,
              "breakbarDamage": 3,
              ...
          },
          ... //more targets
        ]],
        "statsAll": [ //some general stats of the players
          {
              "stackDist": 5853.7578125,
              "distToCom": 6885.62109375,
              "swapCount": 22,
              "missed": 1,
              "evaded": 17,
              "blocked": 11,
              "interrupts": 3,
              "invulned": 28,
              "killed": 3,
              "downed": 1
              ...
          }
        ],
        "support" [ //support actions of the player
          {
              "resurrects": 0,
              "condiCleanse": 33
              "condiCleanseSelf": 27,
              "boonStrips": 47,
              ...
          }
        ],
        ... //other data of the player
    },
    ... //other players
  ],
  "targets:" [
        {
            "name": "Enemy Players",
            "enemyPlayer": true,
            "instanceID": 11111
        },
        ... //other targets
  ],
  "phases": [ //KEEP ALL
  ],
  "mechanics": [ //KEEP ALL
  ],
  "logErrors": [ //KEEP ALL
    "error 1",
    "error 2",
    ...
  ],
  "usedExtensions" [ //KEEP ALL
    {
       //data about extension
    },
    ... //more extensions
  ]
}
```

TODO: find out what is 'phases' and 'mechanics' in power BI file
 - found top level fields with these names in the JSON. Keeping them in full.