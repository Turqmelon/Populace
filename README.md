# Populace
Town management for the modern world.

Populace was originally designed for a survival server before development was halted to work on other things. While a majority of the features are finished, some bugs and other functions were not completed.

### Dependencies
+ MelonEco

### Soft Dependencies
+ PopulaceMarket (Enables plot shops, commercial plots, and sales tax)
+ PopulaceBadges (Adds custom chat prefixes, purely cosmetic)
+ PopulaceChat (Adds chat channels and town chat)
+ PopulaceJoin (Makes join and quit messages town-specific)
+ PopulacePortals (Allows creation of regions to go to specific locations in the world)
+ PopulaceScoreboards (Displays town/rank info on player nametags)
+ PopulaceSpawn (Adds /spawn and /setspawn, as well as handles deaths for town respawning)
+ PopulaceWarzone (Allows creation of PvP areas that are indestructable)

Some of these add-on plugins are currently released, some aren't available to be. They're all pretty simple and work off the Populace API.

Currently Populace only offers flatfile storage, but this can be adapted should it be desired.

### Features
+ Intuitive in-game GUIs and commands for players
+ Developer-first API
+ Virtually grief proof for players not part of a town
+ Per-plot permission systems
+ Town banking system
+ Town reserve system, which is drawn for after the bank to protect from malicious mayors
+ Advanced taxes, customizable by the mayor
+ Town grace period to give new mayors some time to learn how to run a Populace town
+ Town leveling - Unlock features and more land was people join your town
+ Visitor Policies - Choose who can do what within your town borders
+ Rank Management - Customize roles and permissions of each resident of your town
+ Built-in player shops with sales tax support
+ Hilarious town destruction for when towns can't pay their daily upkeep (optional)
+ Town jails to punish naughty residents
+ ... much more

### Commands
+ **/jail <Player> [Time]** - Sends a player to the town jail. They can either wait out the sentence or leave the town
+ **/unjail <PLayer>** - Frees a player from the town jail
+ **/setjail** - Sets the town jail within town land
+ **/board** - Shows the town message board, also shown on login
+ **/grantflight <Town> <Time>** - Admin command. Gives a town the ability to fly within their borders for a short time.
+ **/allow <Player>** - Add a player to the current plot's allow list
+ **/claim** - Claim a plot for your town, or purchase a plot from the town
+ **/forsale <Price>** - Mark a plot of land for sale
+ **/givePlot <Player>** - Allow a specific player to **/claim** a plot within the town
+ **/invite <Player>** - Invite a player to your town
+ **/join <Town>** - Join a public town or accept an invite
+ **/notForSale** - Remove a plot as being for sale
+ **/town (Town)** - View details about your current town, or a specific town
+ **/towns** - View all towns on the server
+ **/unclaim** - Unclaim town land
+ **/visit (Town)** - Travel to the spawn of the specific town, or your own
+ **/newTown <Name>** - Create a new town
+ **/plot** - View details about the plot you're standing in
+ **/resident (Player)** - View details about you or another player
+ **/map (Town)** - Request the map for a town
+ **/visualize** - Visualize the plot you're standing in
+ **/populace (save)** - View details about populace or save all data
+ **/nextNewDay** - Countdown to the next new day of Populace

### Screenshots
![Town Menu](http://i.imgur.com/8Hwaa7t.gif)
/town Menu

![Town Map](http://i.imgur.com/CH4BqD1.png)
Town /map

![Resident Tax](http://i.imgur.com/6hudRpm.png)
![Plot Tax](http://i.imgur.com/TMyuVCT.png)
Mayor Tax Management

![Permissions](http://i.imgur.com/2ZBAmKB.gif)
Permission Management

There's many more features, too. Let me know if you have any questions.
