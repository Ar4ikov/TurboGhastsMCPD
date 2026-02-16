# Admin Configuration

All mod settings are configurable in-game via the `/gai config` GUI. No config files needed.

## Configuration GUI

Run `/gai config` to open the settings panel. Click on any item to modify its value.

### Cost Settings

| Setting | Default | Description |
|---|---|---|
| Registration — Emeralds | 5 | Emeralds required to register a new ghast |
| Registration — Diamonds | 1 | Diamonds required to register a new ghast |
| Rename — Emeralds | 3 | Emeralds required to rename a ghast |
| Rename — Diamonds | 0 | Diamonds required to rename a ghast |
| Stage 2 — Emeralds | 5 | Emeralds for Stage 2 speed upgrade |
| Stage 2 — Diamonds | 2 | Diamonds for Stage 2 speed upgrade |
| Stage 3 — Emeralds | 10 | Emeralds for Stage 3 speed upgrade |
| Stage 3 — Diamonds | 5 | Diamonds for Stage 3 speed upgrade |
| Particles — Emeralds | 3 | Emeralds for changing particle type |
| Particles — Diamonds | 1 | Diamonds for changing particle type |
| Impound Release — Emeralds | 10 | Emeralds to release a ghast from impound |
| Impound Release — Diamonds | 2 | Diamonds to release a ghast from impound |

### Timer Settings

| Setting | Default | Description |
|---|---|---|
| Registration Period | 24 hours | Time before an unregistered ghast is auto-impounded |
| Grace Period | 48 hours | Extended time window when grace mode is active |

### Impound Zone

The impound zone is a rectangular box defined by two corner positions:

1. Stand at the first corner and run `/gai zone set corner1`
2. Stand at the opposite corner and run `/gai zone set corner2`

Both corners must be in the same dimension. The impound zone can be in any dimension (overworld, nether, end, or modded dimensions).

**Important:** The impound zone must be configured before any auto-impounding or manual impounding can occur.

### Grace Period

The grace period provides a longer registration window when first enabled. This is useful when installing the mod on an existing server — it gives players extra time to register their pre-existing ghasts.

- **Grace Active**: Toggle on/off
- **Grace Start Timestamp**: Automatically set when grace is activated

When active, the effective registration period is `max(grace_remaining, registration_period)`.

## Server Statistics

Run `/gai stats` to view a comprehensive overview of all resource transactions on the server:

- Total emeralds and diamonds spent across all categories
- Per-category breakdown (registration, rename, stages, particles, impound release)
- Number of transactions per category
- Current ghast population (total, registered, impounded)
- Combined mileage of all ghasts

All statistics are persisted and survive server restarts.

## Data Storage

All data is stored in the world's `PersistentState` system (inside the world save folder). This means:

- Data is automatically saved with the world
- Backups capture the full mod state
- No external databases or config files needed
