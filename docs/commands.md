# Commands Reference

All commands use the `/gai` root. Player commands are available to everyone; admin commands require **OP level 2** (gamemasters).

## Player Commands

### `/gai tp`

Teleport one of your ghasts to your current location.

- **Without arguments** — opens a GUI showing all your registered ghasts. Click one to teleport it.
- **With argument** — `/gai tp <target>` teleports the ghast directly.

`<target>` accepts:
- License plate number (e.g., `С55600 52`)
- Ghast UUID (full or partial prefix)
- Ghast custom name

**Behavior:**
- After teleportation, the ghast is **frozen for 3 seconds** so you can mount it.
- The ghast unfreezes automatically when you mount it, or after 3 seconds.
- Cooldown: **5 minutes** between teleports (per player).
- Admins have **no cooldown** and can teleport any ghast.
- **Impounded ghasts cannot be teleported.**

---

## Admin Commands

All admin commands require OP level 2 (`/op <player>`).

### `/gai list`

Shows a summary of all tracked ghasts on the server:
- Total count, registered count, impounded count
- Per-ghast: status, name, plate number, short UUID, owner name

### `/gai info <target>`

Opens the admin info GUI for a specific ghast. `<target>` can be a plate number, UUID, or name.

### `/gai player <name>`

Opens a GUI showing all ghasts owned by the specified player.

### `/gai impound <target>`

Manually confiscate a ghast and send it to the impound zone. The ghast will be teleported to the center of the configured impound zone.

- Requires the impound zone to be configured first.
- The ghast's home position is saved before impounding for release.

### `/gai release <target>`

Manually release a ghast from the impound zone. The ghast is teleported back to its saved home position.

### `/gai zone set corner1`

Sets the first corner of the impound zone to your current position. Also records the current dimension.

### `/gai zone set corner2`

Sets the second corner of the impound zone to your current position.

> Both corners must be set in the same dimension. The impound zone is a rectangular box defined by the two corners.

### `/gai config`

Opens the admin configuration GUI where you can adjust:
- Registration cost (emeralds, diamonds)
- Rename cost
- Stage 2 / Stage 3 upgrade costs
- Particle change cost
- Impound release cost
- Registration period (how long before auto-impound)
- Grace period and grace period activation

### `/gai stats`

Opens the server statistics GUI showing:
- **Total resources spent** across all player transactions (emeralds, diamonds)
- **Per-category breakdown**: registration, renaming, stage upgrades, particles, impound releases
- **Transaction counts** for each category
- **Ghast counts**: total tracked, registered, impounded
- **Total mileage** across all registered ghasts

---

## Target Resolution

For commands that accept `<target>`, the following lookup order is used:

1. **License plate number** — exact match
2. **Full UUID** — exact match
3. **Custom name** — case-insensitive exact match
4. **UUID prefix** — partial match (only if exactly one ghast matches)
