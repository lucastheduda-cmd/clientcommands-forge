**Warning:** This mod may contain bugs!

# clientcommands-forge

An unofficial Forge port of [Earthcomputer/clientcommands](https://github.com/Earthcomputer/clientcommands)
(the "Client Commands" mod) for Minecraft 26.2 / Forge 65.0.9.

clientcommands has never had an official Forge or NeoForge build - it's a Fabric-only mod that
leans heavily on Fabric-specific APIs and ~30 subcommands. This port is **not** a full port of the
mod. It's a working scaffold plus the one feature that mattered most for this build: the
enchantment-seed cracker.

## What's included

- **`/cenchant <item> [predicate] [--simulate]`** - predicts and manipulates which enchantments
  you'll get from an enchanting table, by cracking the player entity's RNG seed and the
  enchantment table's XP seed, then (unless `--simulate` is passed) throwing a computed number of
  items to steer the RNG toward a seed that produces enchantments matching your predicate.
- **`/cenchant info`** - feeds the cracker the currently-open enchanting table's clues (replaces
  upstream's GUI overlay/button with a chat command).
- **`/cenchant predict on|off`** - toggles enchant prediction (replaces upstream's `/cconfig`).
- **`/ccrackrng`** - cracks the player entity's RNG seed by throwing items and reading their
  velocities from the resulting entity-spawn packets.
- **`/ctask list`, `/ctask stop <name>`** - minimal task management, enough to back the
  "Cancel"/"Crack" chat links the above commands print.
- The full RNG-desync-detection mixin set (~30 mixins) that the above commands depend on for
  *correctness* - watching nearly every action that can consume the player's random number
  generator (dropping items, eating, equipping, taking damage, anvils, crossbows, mining, mob
  interactions, etc.) so the cracked seed is invalidated rather than silently going stale.

## What's not included

The other ~30 commands from upstream (waypoints, block/entity highlighting, fishing
manipulation, chat utilities, etc.) are not ported. Config is a plain in-memory `Configs` class
(resets on restart) rather than upstream's full "betterconfig" persisted-config system. See the
class-level doc comments on `EnchantmentCracker`, `PlayerRandCracker`, `Configs`, and
`ClientCommandHelper` for the specific adaptations made porting from Fabric to Forge.

## Verification performed

- `./gradlew build` succeeds (compiles, assembles, embeds seed-cracking dependencies via JarJar).
- `./gradlew runClient` boots to the main menu with no Mixin application failures across all 35
  mixins, and Forge confirms the mod loaded.
- **Not verified**: actually joining a world and running `/cenchant`/`/ccrackrng` in-game, since
  this was built without a Minecraft/CurseForge install on the build machine. Please report back
  if either command misbehaves in practice - the underlying seed-cracking math is copied verbatim
  from upstream, but the Forge-side plumbing (command registration, mixin injection points,
  RNG-desync event wiring) is new and could have edge cases that only show up in real play.

## Building

Requires Java 25.

```
./gradlew build
```

Produces two jars in `build/libs/`:
- `clientcommandsforge-<version>-slim.jar` - without embedded dependencies (for use with a
  separate mod-loader-managed seed-cracking library, if you have one).
- `clientcommandsforge-<version>.jar` - **this is the one to install** - embeds the seed-cracking
  math libraries (`com.seedfinding:mc_core`, `com.seedfinding:mc_seed`,
  `com.seedfinding:latticg`) and MixinExtras via Forge's JarJar.

## License

LGPL-3.0-or-later, matching the upstream mod's license.
