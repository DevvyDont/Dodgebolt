# Dodgebolt

This is a Paper plugin that is meant to be a recreation of the Dodgebolt finale gamemode in Noxcrew's Minecraft
Championship.

## Description

This project aims to provide a way to play the gamemode with friends as there is no public way to
do so. This plugin also comes with a couple extra features such as dynamic team colors that affect the arena color
as well as player chat colors, player stat tracking that can be viewed when a player wins a game, and a couple rule
changes such as a 'win by 2' mechanic that works similarly to tennis. It is also possible to create custom stadiums
and arenas that will have dynamic color changing blocks, however the plugin is mainly focused to work with the default
arena/stadium.

## I wanna play!

This installation process assumes that you know how to setup a basic Minecraft server using Paper.

### Things you will need

* A Minecraft server using the [latest version of Paper](https://papermc.io/downloads)
* [WorldEdit for Spigot/Bukkit](https://dev.bukkit.org/projects/worldedit)

### Installing

* Download the latest version of the plugin + server files in the [releases tab](https://github.com/DevvyDont/Dodgebolt/releases)
* Setup the Minecraft server using paper just like any normal Minecraft server. As a precautionary step, feel free to put all the server files provided via the latest release beforehand.
* Place the Dodgebolt.jar in the plugins folder, also make sure you have WorldEdit installed alongside Dodgebolt.
* If you haven't already, combine the server files provided in the releases tab with the server folder. This provides an empty void world, and the WorldEdit schematics that contains the arena and stadium.
* Run the server! You should be good to go

### Config/Tweaks

* Checkout the config.yml for a few settings that can be tweaked, which includes things such as only letting ops start the game, and the round win limit.
* Shift clicking the team switch signs will dynamically change a team color
* There are no commands in this plugin as everything should be able to function automatically!

### Help i broke something :(

If anything seems to go wrong, a simple `/reload confirm` should fix any issues, as the plugin will completely reset
including if part of the arena gets destroyed or a sign breaks for whatever reason

### License

This project is licensed under the MIT License - see the LICENSE file for details

### Acknowledgments

* Inspired by/Recreation of the Dodgebolt minigame from [Minecraft Championship by Noxcrew](https://noxcrew.com/mcc)