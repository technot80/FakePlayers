# FakePlayersFolia

A Minecraft plugin for Folia 1.21+ that simulates fake players to make servers appear more active. When real players join and see an empty server, they often leave quickly. This plugin populates the tab list with fake players to create the illusion of an active community.

## Features

- **Fake Players in Tab List** - Uses NMS packet manipulation to show fake players without physical entities
- **Join/Quit Messages** - Broadcasts realistic join/quit messages when fake players are added/removed
- **Welcome Messages** - Fake players send welcome messages when real players join
- **/msg Interception** - Intercepts private messages to fake players with realistic responses
- **Activity Scheduler** - Randomly adds/removes fake players over time (only active when minimum real players online)
- **PlaceholderAPI Support** - Expose fake player counts via placeholders
- **Fully Folia Compatible** - Uses Folia's schedulers for async operations

## Requirements

- **Server:** Folia 1.21.11+
- **Java:** 21+
- **Soft Dependencies:** PlaceholderAPI (optional)

## Installation

1. Download the latest release
2. Place `FakePlayersFolia-1.0.0-reobf.jar` in your server's `plugins` folder
3. Start the server
4. Configure settings in `plugins/FakePlayersFolia/config.yml`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/fp add [name]` | Add a fake player (random name if not specified) | `fakeplayers.add` |
| `/fp remove <name\|all>` | Remove a fake player or all fake players | `fakeplayers.remove` |
| `/fp list` | List all online fake players | `fakeplayers.list` |
| `/fp status` | Show real/fake player counts | `fakeplayers.status` |
| `/fp reload` | Reload all configuration files | `fakeplayers.reload` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `fakeplayers.admin` | Full access to all commands | op |
| `fakeplayers.add` | Add fake players | op |
| `fakeplayers.remove` | Remove fake players | op |
| `fakeplayers.list` | List fake players | op |
| `fakeplayers.status` | View status | op |
| `fakeplayers.reload` | Reload configuration | op |

## Configuration

### config.yml

```yaml
# Server connection (for reference)
server:
  host: "localhost"
  port: 25565

# Activity settings
activity:
  min-real-players: 1    # Minimum real players before fakes activate
  max-fake-players: 20   # Maximum fake players at once

# Join behavior
join:
  delay-min: 30          # Minimum seconds between joins
  delay-max: 120         # Maximum seconds between joins
  chance: 100            # Percent chance per cycle (0-100)

# Quit behavior
quit:
  enabled: true
  delay-min: 300         # Minimum seconds before auto-quit
  delay-max: 900         # Maximum seconds before auto-quit

# Welcome message behavior
welcome:
  enabled: true
  chance: 50             # Percent chance to welcome (0-100)
  delay-min: 2           # Minimum seconds after join
  delay-max: 8           # Maximum seconds after join
  max-concurrent: 3      # Max fake players that welcome at once
  cooldown: 60           # Cooldown before same player welcomed again

# Bot settings
bot:
  invisible: true
  invulnerable: true
  no-gravity: true
  no-collision: true
  hide-from-players: true

# Debug mode
debug: false
```

### name-list.yml

Contains the pool of names for fake players. Names are randomly selected from this list. Edit to customize your fake player names.

### greetings.yml

Customize welcome messages and /msg responses:

```yaml
# Messages for first-time players
first-join:
  - "Hey {player}! Welcome to the server!"
  - "Welcome {player}! Nice to see a new face!"

# Messages for returning players
rejoin:
  - "Welcome back {player}!"
  - "Hey {player}, good to see you again!"

# Responses when someone /msg's a fake player
msg-responses:
  - "afk"
  - "busy"
  - "ttyl"
```

## PlaceholderAPI

Placeholders available when PlaceholderAPI is installed:

| Placeholder | Description |
|-------------|-------------|
| `%fakeplayers_count%` | Number of fake players currently online |
| `%fakeplayers_total_online%` | Real + fake players combined |
| `%fakeplayers_real_online%` | Only real players |
| `%fakeplayers_max%` | Maximum fake players allowed |
| `%fakeplayers_min_real%` | Minimum real players required |

## Building

```bash
gradle clean reobfJar
```

Output: `build/libs/FakePlayersFolia-1.0.0-reobf.jar`

## Technical Notes

- Uses NMS packet manipulation (`ClientboundPlayerInfoUpdatePacket`) to show fake players in the tab list
- Fake players have no physical entity in the world (invisible, no collision)
- All async tasks use Folia's `GlobalRegionScheduler` for compatibility
- The `bot.*` settings in config currently have no effect as fake players are packet-based only

## License

See [LICENSE](LICENSE) file.
