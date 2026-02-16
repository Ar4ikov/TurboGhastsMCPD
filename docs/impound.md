# Impound System

## Overview

The impound system is the enforcement mechanism of the mod. Ghasts that aren't registered in time, or that an admin manually confiscates, get teleported to a designated impound zone.

## Auto-Impounding

When an unregistered ghast's deadline expires:

1. The ghast is teleported to the center of the impound zone
2. Its `isImpounded` flag is set to `true`
3. Its home position (before impounding) is saved for later release
4. License plates are cleaned up from all dimensions
5. The owner (if any) receives a chat message about the confiscation
6. All online players receive a notification

## Manual Impounding

Admins can manually impound any ghast:

```
/gai impound <target>
```

This works the same as auto-impounding. The impound zone must be configured first.

## Impound Zone

The zone is a rectangular box defined by two corner coordinates in the same dimension.

### Setup

1. Stand at corner 1: `/gai zone set corner1`
2. Stand at corner 2: `/gai zone set corner2`

The zone dimension is determined by where you stand when setting corner 1.

### Containment

The mod enforces containment every 3 ticks:

- If an impounded ghast moves outside the zone boundaries, it is teleported back to the center
- Any players riding an impounded ghast are **forcibly dismounted**
- This applies to both player-controlled and AI-driven movement

**Impounded ghasts cannot:**
- Leave the impound zone
- Have their parameters edited (name, stage, particles)
- Be teleported via `/gai tp`

## Releasing

### Admin Release (Free)

```
/gai release <target>
```

The ghast is teleported back to its saved home position. No cost.

### Player Release (Paid)

When a player Shift+RMBs an impounded ghast, a release payment GUI opens:

- **If registered**: Pay the impound release cost (default: 10 emeralds + 2 diamonds)
- **If unregistered**: Pay release cost + registration cost combined

After payment:
- The ghast is teleported home
- Its impounded status is cleared
- License plates are recreated

## Technical Details

### Home Position

When a ghast is impounded, its current coordinates and dimension are stored as the "home position." On release, the ghast is teleported back to this exact location and dimension.

### Cross-Dimension Teleportation

The impound system handles teleportation between dimensions. A ghast in the Nether will be teleported to the overworld impound zone (or wherever it's configured), and back to the Nether on release.

### TextDisplay Cleanup

License plate TextDisplay entities are removed from **all loaded dimensions** before teleportation and recreated at the destination. This prevents ghost plates from lingering in the wrong dimension.
