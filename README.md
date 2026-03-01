# FakePlayersFolia

A Minecraft plugin for Folia 1.21+ that simulates fake players to make servers appear more active. When real players join and see an empty server, they often leave quickly. This plugin populates the tab list with fake players to create the illusion of an active community.

## Features

- **Fake Players in Tab List** - Uses NMS packet manipulation to show fake players without physical entities
- **Join/Quit Messages** - Broadcasts realistic join/quit messages when fake players are added/removed
- **Welcome Messages** - Fake players send welcome messages when real players join
- **AI Chat Support** - Give fake players AI-powered conversations using [AgentChatAPI](https://github.com/technot80/AgentChatAPI)
- **Activity Scheduler** - Randomly adds/removes fake players over time (only active when minimum real players online)
- **PlaceholderAPI Support** - Expose fake player counts via placeholders
- **Fully Folia Compatible** - Uses Folia's schedulers for async operations

## Requirements

- **Server:** Folia 1.21.11+
- **Java:** 21+
- **Soft Dependencies:** PlaceholderAPI (optional), [AgentChatAPI](https://github.com/technot80/AgentChatAPI) (optional, for AI chat)

## Installation

1. Download the latest release
2. Place `FakePlayersFolia-1.0.0-reobf.jar` in your server's `plugins` folder
3. (Optional) Download [AgentChatAPI](https://github.com/technot80/AgentChatAPI/releases) for AI chat features
4. Start the server
5. Configure settings in `plugins/FakePlayersFolia/config.yml`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/fp add [name]` | Add a fake player (random name if not specified) | `fakeplayers.add` |
| `/fp remove <name\|all>` | Remove a fake player or all fake players | `fakeplayers.remove` |
| `/fp list` | List all online fake players | `fakeplayers.list` |
| `/fp status` | Show real/fake player counts | `fakeplayers.status` |
| `/fp reload` | Reload all configuration files and personalities | `fakeplayers.reload` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `fakeplayers.admin` | Full access to all commands | op |
| `fakeplayers.add` | Add fake players | op |
| `fakeplayers.remove` | Remove fake players | op |
| `fakeplayers.list` | List fake players | op |
| `fakeplayers.status` | View status | op |
| `fakeplayers.reload` | Reload configuration | op |

## AI Chat (Optional)

Fake players can have AI-powered conversations when AgentChatAPI is installed.

### Setup

1. Install [AgentChatAPI](https://github.com/technot80/AgentChatAPI) on your server
2. Configure your API key in AgentChatAPI's `config.yml`
3. Set `ai.enabled-ai-count` in FakePlayersFolia's `config.yml`

### Configuration

```yaml
ai:
  # Number of fake players that can use AI chat (0 to disable)
  enabled-ai-count: 5
  # Chance for an online fake player to be selected for AI (0-100)
  selection-chance: 50
  # Minimum real players online before AI fake players start chatting
  min-real-players: 1
  # Delay in ticks between AI chat messages (20 ticks = 1 second)
  chat-delay-min: 100
  chat-delay-max: 300
  # Chance an AI fake player will respond to real player chat (0-100)
  response-chance: 30
  # Chance AI fake players will chat amongst themselves (0-100)
  self-chat-chance: 20
```

### Personalities

Each fake player gets an AI personality. On first run, personalities are automatically generated (one per name in your name list). You can edit these in:

```
plugins/FakePlayersFolia/personalities/
```

Each file is named after the fake player (e.g., `steve.txt`, `alex.txt`) and contains their personality description. Edit these files to customize how each fake player acts.

The global system prompt ensures AI fake players:
- Never reveal they're AI
- Never write code or solve math
- Avoid politics and controversial topics
- Stay friendly and never use swear words
- Keep responses short and natural

Reload personalities after editing:
```
/fp reload
```

## Configuration

### config.yml

```yaml
# Activity settings
activity:
  # Run fake player activity even when no real players are online
  always-active: false
  # Minimum real players required before fake players become active
  min-real-players: 1
  # Maximum fake players allowed online at once
  max-fake-players: 20
```

When `always-active: true`, fake players will continue joining and quitting randomly even when no real players are online. When `false` (default), fake players only become active when at least `min-real-players` are online.

### name-list.yml

Contains the pool of names for fake players. Names are randomly selected from this list. Each name gets its own AI personality file.

### greetings.yml

Customize welcome messages:

```yaml
# Messages for first-time players
first-join:
  - "Hey {player}! Welcome to the server!"
  - "Welcome {player}! Nice to see a new face!"

# Messages for returning players
rejoin:
  - "Welcome back {player}!"
  - "Hey {player}, good to see you again!"
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
- AI chat uses AgentChatAPI - if not installed, AI features are silently disabled
- Personalities are generated per-name from the name list and persist across restarts

## License

See [LICENSE](LICENSE) file.
