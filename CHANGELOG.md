# Changelog

## 0.1.7

- Added named `Home Setter` support so admins can create setters such as `Casa` or `Checkpoint`
- Added `Handcuffs` as a police item using the vanilla `lead` texture
- Added timed handcuff application with a centered progress bar and temporary detained state

## 0.1.6

- Added offline player resolution by cached player name for time-limit administration
- Added a configurable weekly pause window for the time-limit system
- Improved time-limit system status output to show effective active or paused state

## 0.1.5

- Added clickable world votes for `day`, `night`, and `clear weather`
- Added command cooldown handling for `/tpa`, `/spawn`, and `/home`
- Added the `Home Setter` item for setting player homes by right-click
- Added forced PvP event controls for individual players and global events
- Added optional admin validation bypass through config
- Added real-time daily playtime limits based on `America/Bogota`
- Added separate weekday and weekend limits for `PAID` and `UNPAID` player categories
- Added persistent per-player playtime tracking, category storage, bypass flags, and daily reset state
- Added admin commands to manage time-limit system state, enforcement, categories, bypass, resets, and reloads

## 0.1.4

- Added compatibility handling so Easy NPC merchants are not blocked by vanilla villager trading restrictions
- Kept vanilla `Villager` and `WanderingTrader` trade blocking separate from Easy NPC merchant interactions

## 0.1.3

- Added optional JEI integration for the Recall Potion brewing recipe
- Registered the Recall Potion brewing recipe in JEI as `awkward potion + nether star`
- Added JEI plugin metadata and optional compile-time API integration for Fabric `1.21.11`

## 0.1.2

- Changed the Recall Potion to return players to their personal respawn point when available
- Added fallback from the Recall Potion to world spawn when no valid personal respawn point exists
- Added config options to disable vanilla `Villager` trading and `WanderingTrader` trading independently

## 0.1.1

- Removed all elytra flight restrictions
- Changed the default combat tag duration to `15` seconds
- Added hostile mob combat tagging for players, including hostile projectiles
- Changed radar flow to one radar per player
- Added radar assignment validation so a hunter must have a radar in inventory
- Added automatic radar expiration cleanup that removes the radar item when the assignment ends

## 0.1.0

First alpha release of `Culiatum RP`.

- Added base PvP system for Fabric `1.21.11`
- Added combat tagging on player-vs-player damage
- Added command blocking during combat for teleport-style commands such as `tpa`, `tpaccept`, `tpdeny`, and `spawn`
- Added `Recall Potion` as a custom drinkable item that returns the player to spawn
- Added brewing support for `awkward potion + nether star -> Recall Potion`
- Added configurable `Radar` item for bounty and hunter-style missions
- Added admin commands to give radars, assign targets, clear assignments, and inspect radar status
- Added Mod Menu metadata, repository links, and alpha project description
- Established repository conventions for clean branching and worktree-based development
