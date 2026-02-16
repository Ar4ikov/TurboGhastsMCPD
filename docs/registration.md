# Registration System

## Overview

Every Happy Ghast that exists in a loaded chunk is automatically detected and tracked by the mod. Once detected, the ghast is placed on a registration watchlist with a countdown timer.

Players must register their ghasts before the timer expires, or the ghast will be automatically impounded (confiscated).

## Detection

The `GhastTrackingHandler` scans loaded chunks on every server tick, looking for Happy Ghast entities that aren't yet in the registry. When a new ghast is found:

1. A new `GhastVehicleData` entry is created
2. A registration deadline is set (current time + registration period)
3. The ghast appears in the unregistered list

> **Note:** Ghasts in unloaded chunks won't be detected until a player loads the chunk. Once loaded, the timer starts.

## Registration Flow

To register a ghast, a player must **Shift + Right-Click** on it. This opens the registration wizard:

### Step 1: Name

An anvil GUI lets you type a custom name for your ghast. Default: "Гастик".

### Step 2: Harness Color

Pick from all 16 Minecraft dye colors. The chosen color is applied to the ghast's harness.

### Step 3: License Plate

7 randomly generated plate numbers are presented. Each plate follows the Russian format (e.g., `С55600 52`). Pick one.

Plate numbers are unique across the server — no duplicates allowed.

### Step 4: Payment

Confirm the registration cost (configurable by admin, default: 5 emeralds + 1 diamond). The resources are consumed from your inventory.

### After Registration

- The ghast receives a colored harness
- License plates (TextDisplay entities) appear on the front and back
- Speed Stage 1 is applied
- The ghast is marked as registered with you as the owner

## Ghast Info Menu

After registration, Shift+RMB on the ghast opens the info/customization panel:

### Owner View

As the ghast's owner, you can:
- **Rename** — change the custom name (costs emeralds)
- **Change harness color** — pick a new color (free)
- **Switch/buy speed stages** — toggle between unlocked stages or purchase new ones
- **Change particles** — select particle trail effects (costs emeralds + diamonds)

### Read-Only View

Other players who Shift+RMB the ghast can view info but **cannot modify** anything:
- Name, owner, plate number
- Current stage and particles
- Mileage

### Menu Locking

When a menu is opened for editing:
- The ghast **freezes** (AI disabled) until the menu is closed
- Only **one editor at a time** — other players see read-only view
- **Admin priority** — if an admin opens the menu, the current editor is kicked out
- If the player disconnects, the lock is released and the ghast unfreezes

## Mileage

The mod tracks distance traveled (in blocks) while a player is riding the ghast. Mileage is displayed in the info menu and is purely cosmetic — it serves as a fun stat tracker.

## Death & Insurance

If a registered ghast dies:
1. The ghast is permanently removed from the registry
2. License plates are despawned
3. The owner receives a condolence message in chat
4. A **15% refund** of all invested resources is given (minimum 1 emerald + 1 diamond for basic registration)
5. The ghast **cannot be restored**
