# Culiatum RP

`Culiatum RP` is a server-side Fabric mod for `1.21.11` focused on PvP rules and gameplay systems for the Culiatum server. This early `alpha` establishes the core foundation for combat control, limited escape tools, and bounty-style missions.

## Project Status

- Current version: `0.1.8-alpha`
- Target loader: `Fabric Loader 0.18.4`
- Target Java version: `21`
- Compatible with `Mod Menu` through metadata in `fabric.mod.json`

## What This Alpha Includes

- Combat-time blocking for configured teleport commands
- Combat tagging for both PvP and hostile mob encounters
- A drinkable `Recall Potion` that returns the player to spawn
- A command-configurable `Radar` item for bounty target assignment
- One-radar-per-player mission flow with automatic expiration cleanup
- Action bar radar feedback with cooldown
- Clickable world votes for `day`, `night`, and `clear weather`
- Command cooldown handling for `/tpa`, `/spawn`, and `/home`
- A `Home Setter` item for setting named homes by right-click
- `Handcuffs` for temporary police detainment
- A `Police Baton` for light police crowd-control on hit
- Forced PvP event controls for individual players and global events
- A real-time daily playtime limit system with persistent player tracking
- Anvil-only support for carrying over existing creative-level enchantments
- Basic configuration through `config/culiatum-rp.properties`

## Mod Vision

This mod is built around the specific needs of the Culiatum server. Its goal is not to be a generic PvP framework, but to provide clear rules, administrable tools, and items with fixed gameplay roles tailored to the server experience.

Many item and system properties can be adjusted through commands and configuration, but their core gameplay functions are not designed to be fully replaced by datapacks or external configs.

## Relationship With Culiatum Economy

`Culiatum RP` is designed to work alongside `culiatum-economy`, although both mods are not strictly required together. The long-term idea is for them to share progression, missions, administrative tools, and server systems without forcing a hard dependency between them.

## Compatibility

- Works without `culiatum-economy`
- Compatible with `Easy NPC` merchant interactions
- Not intended to be compatible with mods such as `aicheye combat tagging`, because they cover a very similar feature set
- Using another combat-tagging or combat-command-blocking mod may create overlapping behavior

## Admin Commands

The command-editable systems currently start with:

```mcfunction
/culiatumrp radar give <player>
/culiatumrp radar set <hunter> <target> <minutes> [label]
/culiatumrp radar clear <hunter>
/culiatumrp radar status <hunter>
/culiatumrp homesetter give <player> [name]
/culiatumrp handcuffs give <player>
/culiatumrp baton give <player>
/culiatumrp pvp enable <player>
/culiatumrp pvp disable <player>
/culiatumrp pvp enableall
/culiatumrp pvp disableall
/culiatumrp pvp status <player>
/culiatumrp timelimit system status
/culiatumrp timelimit system enable
/culiatumrp timelimit system disable
/culiatumrp timelimit enforcement enable
/culiatumrp timelimit enforcement disable
/culiatumrp timelimit enforcement status
/culiatumrp timelimit opbypass enable
/culiatumrp timelimit opbypass disable
/culiatumrp timelimit opbypass status
/culiatumrp timelimit player status <player>
/culiatumrp timelimit player category <player> paid|unpaid
/culiatumrp timelimit player bypass <player> <true|false>
/culiatumrp timelimit player reset <player>
/culiatumrp timelimit resetall
/culiatumrp timelimit reload
```

## Daily Time Limits

The time-limit system uses real-world calendar time from `America/Bogota`, not the in-game day cycle.

It supports:

- Separate weekday and weekend limits
- Separate `PAID` and `UNPAID` player categories
- Persistent usage tracking across restarts
- Automatic midnight resets in Bogota time
- Optional automatic weekly pause windows, such as a full weekend pause
- Optional admin bypass
- Offline admin recovery by cached player name for players who are not currently connected

Administrative player commands resolve targets in this order:

- online player by exact name
- cached player profile from `usercache.json`

That means a player must have joined the server at least once before they can be managed while offline.

## Config Reference

The main config file is:

```text
config/culiatum-rp.properties
```

Important variables currently exposed there include:

### Combat and command control

- `combat_tag_seconds`: duration of the regular combat tag
- `blocked_command_prefixes`: configured command roots blocked during combat
- `tpa_cooldown_seconds`: cooldown for `/tpa`
- `spawn_cooldown_seconds`: cooldown for `/spawn`
- `home_cooldown_seconds`: cooldown for `/home`
- `disable_home_set_commands`: optional hard block for `/home set` and `/sethome`
- `op_bypass_validations`: allows OPs to bypass command and time-limit validations

### Radar and mission items

- `radar_cooldown_seconds`: cooldown between radar uses

### Home and police items

- `home_setter`: admin item flow for setting named homes
- `handcuffs`: temporary detainment item using the vanilla `lead` texture
- `police_baton`: light police control item using the vanilla `stick` texture

### Enchanting behavior

- creative-level enchantments can be carried forward through the anvil only when the donor item already has the higher level
- enchanting table behavior remains vanilla

### Daily time-limit system

- `system_enabled`: manual master switch for the time-limit system
- `enforcement_enabled`: controls whether limit enforcement can kick players
- `timezone`: real-world timezone used for daily resets and weekly pause windows
- `weekday_paid_seconds`: weekday daily limit for `PAID` players
- `weekday_unpaid_seconds`: weekday daily limit for `UNPAID` players
- `weekend_paid_seconds`: weekend daily limit for `PAID` players
- `weekend_unpaid_seconds`: weekend daily limit for `UNPAID` players
- `weekly_pause_enabled`: enables the automatic weekly pause window
- `weekly_pause_start_day`: start day for the weekly pause, such as `FRIDAY`
- `weekly_pause_start_time`: start time for the weekly pause, such as `18:00`
- `weekly_pause_end_day`: end day for the weekly pause, such as `MONDAY`
- `weekly_pause_end_time`: end time for the weekly pause, such as `00:00`
- `limit_action`: current enforcement action setting
- `kick_message`: message used when a player reaches the enforced daily limit

Default weekly pause behavior can be configured to suspend the entire time-limit system between specific real-world calendar points without clearing stored playtime.

### Trading restrictions

- `disable_villager_trading`: blocks vanilla `Villager` trading when enabled
- `disable_wandering_trader_trading`: blocks vanilla `WanderingTrader` trading when enabled

## Development and Branch Workflow

The main branch should remain readable and auditable. The repository convention is:

- `main` for the stable project line
- `feature/*` for new features
- other clear prefixes when needed, such as `fix/*` or `docs/*`
- avoid autogenerated prefixes such as `codex/*`

For new work, the preferred flow is to develop from dedicated `worktrees` and merge back into `main`.

## Local Build

```powershell
.\gradlew.bat build
```

The remapped jar is generated at:

```text
build/libs/culiatum-rp-0.1.8.jar
```
