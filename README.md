<p align="center">
  <img src="docs/assets/readme/couchcraft-mark.png" alt="Couchcraft logo: four player-coloured panes in a dark frame" width="96" height="96" />
</p>

<h1 align="center">Couchcraft Omega</h1>

<p align="center"><strong>An enhanced fork of <a href="https://github.com/zortos293/Couchcraft">Couchcraft</a>: real split-screen co-op for Minecraft Java. One game, one window, up to four players.</strong></p>

<p align="center">
  <a href="https://github.com/Vasissualiyp/CouchcraftOmega/releases/latest">
    <img src="https://img.shields.io/badge/Download-Mod_jar-FFC400?style=for-the-badge&logo=github&logoColor=white&labelColor=111111" alt="Download the mod" />
  </a>
  <img src="https://img.shields.io/badge/Minecraft-26.3-16A05A?style=for-the-badge&labelColor=111111" alt="Minecraft 26.3" />
  <a href="https://fabricmc.net/use/"><img src="https://img.shields.io/badge/Fabric_Loader-0.19.5+-DBD0B4?style=for-the-badge&labelColor=111111" alt="Fabric Loader 0.19.5 or newer" /></a>
  <a href="https://x.com/Zortosdev"><img src="https://img.shields.io/badge/Follow-@Zortosdev-FFFFFF?style=for-the-badge&logo=x&logoColor=white&labelColor=111111" alt="Follow @Zortosdev on X" /></a>
</p>

<p align="center">
  <a href="https://github.com/Vasissualiyp/CouchcraftOmega/releases"><img src="https://img.shields.io/github/downloads/Vasissualiyp/CouchcraftOmega/total?style=for-the-badge&label=Downloads&color=FFC400&labelColor=111111" alt="Total downloads" /></a>
  <a href="https://github.com/Vasissualiyp/CouchcraftOmega/stargazers"><img src="https://img.shields.io/github/stars/Vasissualiyp/CouchcraftOmega?style=for-the-badge&label=Stars&color=2F5BFF&labelColor=111111" alt="GitHub stars" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-E5372B?style=for-the-badge&labelColor=111111" alt="MIT license" /></a>
</p>

<p align="center">
  <a href="#whats-different-in-omega">What's new</a> ·
  <a href="#get-couchcraft-omega">Download</a> ·
  <a href="#play">Play</a> ·
  <a href="#inside-the-mod">Features</a> ·
  <a href="#controller-settings">Controllers</a> ·
  <a href="#how-it-works">How it works</a> ·
  <a href="#build-from-source">Build</a>
</p>

<p align="center">
  <img src="docs/assets/readme/four-players.jpg" alt="Four Minecraft players in one window, split into quarters, each with their own view, hotbar and colour-coded border." width="100%" />
</p>

<p align="center"><em>Four players, one Minecraft window. A real in-game capture, not a mockup.</em></p>

I got tired of co-op mods that start a second copy of the game, tile two windows, and fall over the moment
something goes wrong. So I built split screen into the game itself. Couchcraft runs every player inside **one
Minecraft client**: one process, one window, one world. Plug in a controller, press START, and the screen splits,
like the console editions used to.

> [!NOTE]
> There is no second launcher, no second client and no second window. Each extra player is a real player on your
> world's built-in server, with their own camera, HUD, menus, inventory and progress, all inside the same game.

> [!IMPORTANT]
> Couchcraft is a client mod for **Minecraft Java 26.3** on **Fabric Loader**. You don't need Fabric API. Extra
> players can join any singleplayer world, including one you've opened to LAN. They can't join online servers,
> because every player there needs their own Minecraft account.

> [!WARNING]
> This is an early release. It has been tested on the OpenGL renderer with up to four players. Sodium is **experimental**
> (see below) and Iris doesn't work, and every extra player renders the world again, so lower your render distance if the frame rate drops.

> [!NOTE]
> **Couchcraft Omega is an enhanced fork** of [Couchcraft](https://github.com/zortos293/Couchcraft) by
> [Zortos](https://x.com/Zortosdev), which does all the hard work of single-client split screen. Omega keeps that
> code and its MIT licence and adds the features below. It installs in place of Couchcraft (it declares that it
> `provides` `couchcraft`, so mods that depend on Couchcraft keep working) and must not be installed together
> with it. Changes that fit upstream are also offered back to the original project.

## What's different in Omega

- **Distant Horizons support.** Distant Horizons 3.3.4 works in split screen. Without help, its background threads
  read whichever player was swapped in last, so LOD detail flickered, lightmaps and fog leaked between
  dimensions, and the Nether looked wrong. Omega publishes every player's position to Distant Horizons, so each
  view gets full detail around its own player; fixes the lightmap and fog per dimension; and in the Nether adds
  fog that covers LODs and extra vertical detail so ceilings don't stretch into dark curtains. It is only active
  when Distant Horizons is installed. [Details](#distant-horizons)
- **Shift and Ctrl on the controller.** In menus, hold **LB** for Shift and **RB** for Ctrl, for the keyboard
  shortcuts of vanilla and other mods (inventory sorting, "move all matching" buttons and so on).
- **A controller button map.** Hold **BACK** to see a pixel-art controller with a line from every button to what it
  does, one set of labels for the world and one for menus. Mods can add their own labels through an API, and players
  can rename or hide any label in `config/couchcraft-button-map.properties`. [Details](#button-map)
- **Sodium (experimental).** Couchcraft refused to start with Sodium installed. Sodium keeps its renderer on each
  `LevelRenderer`, which Couchcraft already swaps per player, so Omega no longer blocks it. In a scripted session
  with Sodium 0.9.3-beta.5 on 26.3 (Vulkan backend), 2, 3 and 4 players rendered correctly. Longer play, block
  updates seen from several views, and performance have not been tested yet, so expect rough edges. Iris is still
  blocked.
- Planned: more Sodium testing, more than four players, an on-screen keyboard for chat and signs.

## Get Couchcraft Omega

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.3.
2. Download `couchcraft-omega-<version>.jar` from [Releases](https://github.com/Vasissualiyp/CouchcraftOmega/releases/latest).
3. Put it in your `.minecraft/mods` folder and start the game with the Fabric profile.

Any controller that SDL3 recognises works: Xbox, DualSense / DualShock, Switch Pro, 8BitDo and most others.
Minecraft already ships SDL3, so there's nothing extra to install.

## Play

1. Open a singleplayer world.
2. Plug in a controller and press **START**. The screen splits and asks *Who's playing?*
3. Pick your profile and press **A**. The profile you used last time on that controller is already selected, so
   it's usually just START, A. Player 2 spawns next to you with everything they had last time.
4. Do the same for players 3 and 4. Two players stack top and bottom (switchable to side by side); three get a
   wide top view and two below; four get a quad.
5. To leave, hold **BACK + START**. Everyone else keeps playing.

| Action | Controller |
| --- | --- |
| Move / look | Left stick (analog) / right stick |
| Jump · Drop · Pick block · Inventory | A · B · X · Y |
| Attack / Use | RT / LT |
| Hotbar | LB / RB |
| Sprint · Sneak (toggle) | L3 · R3 |
| Pause · Player list | START · BACK |
| Perspective · Swap hands | D-pad ↑ · D-pad ↓ |
| **In menus** | Stick moves a cursor, A click, X shift-click, Y right-click, B back, D-pad nudges one slot, hold LB for Shift and RB for Ctrl |
| **Button map** | Hold BACK to see what each button does (also in menus) |
| **Leave the game** | Hold BACK + START |

**Player 1** can play on keyboard and mouse, a controller, or both at once. Press **BACK** on any free
controller to make it P1's, at any time, also to switch P1 from one controller to another. Keyboard and mouse
never switch off for P1. Only P1 can use them.

| Shortcut | Does |
| --- | --- |
| <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd> | Open the controller settings (also from the title screen) |
| <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>J</kbd> | Add a player without a controller; they can pick one up with START later |
| <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>K</kbd> | Remove the last player |
| <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>L</kbd> | Switch the two-player layout: stacked ↔ side by side |

## Inside the mod

These are captures from the game running the mod.

### Everyone gets their own menus

![Player 2 browsing the creative inventory with a controller cursor in the bottom half of the screen, while Player 1 keeps playing in the top half.](docs/assets/readme/guest-inventory.jpg)

Each player has their own inventory, chests, pause menu and death screen, drawn in their part of the screen.
Controller players get a cursor in their own colour.

### Different worlds, same couch

![Two players side by side: one in an Overworld forest by a river, the other in the Nether next to lava.](docs/assets/readme/nether-split.jpg)

Players don't have to stay together. One of you can be in the Nether while the others build in the Overworld.

### Profiles: your save comes back with you

Every player is a real player on your world, so their inventory, armour, XP, advancements, statistics and spawn
point are saved with the world, the same way as in multiplayer. Guests keep that save in a **profile**:

- When you join, pick your profile. Couchcraft remembers which controller each profile last played on and
  suggests it (★), so next time it's just START, A.
- Make a new profile from the picker, or rename one. On a controller, type with the on-screen keyboard; on a
  keyboard, just type.
- **Renaming keeps your save.** A profile has a fixed ID that never changes, and that ID is what your worlds save you
  under. Rename *Player2* to *Steve* and Steve still has Player2's diamonds.
- Each profile has its own save in every world you play, so the same profile can be in three different worlds.

Profiles live in `config/couchcraft-profiles.json`. The host plays as their own Minecraft account, as always.

### House rules

- **Pause is a vote.** The world only freezes when everyone is in a pause menu. One player checking the menu
  doesn't stop the others.
- **Every sound once.** Each player's copy of the world hears the same sounds, so Couchcraft drops the duplicates and
  plays each sound from the nearest player's point of view.
- **Leaving is personal.** When a guest picks *Save and Quit*, gets kicked, or holds BACK + START, only that guest
  leaves. When the host quits, everyone's progress is saved.
- **Guests share the cheats.** If the world has cheats on, guests can use commands too.
- **Controllers count as input.** Minecraft drops to 30 FPS, then 10, when it thinks nobody is playing. Controller
  input now counts as playing, so a couch on controllers never gets throttled.

## Controller settings

Open them from the pause menu (**Couch Co-op…**) or with <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>.

<p align="center">
  <img src="docs/assets/readme/settings.jpg" alt="The Couch Co-op settings screen open in Player 1's half of the screen, listing each player with their controller and a Vibrate button." width="100%" />
</p>

- **Profiles.** Every saved profile, who's playing on it right now, *Rename* (and delete), and *New profile*.
- **Players.** Every player with their colour and what they play with. Pick a controller for any player; if
  another player holds it, the two swap. P1's *Keyboard only* means no controller. Add or remove players here.
- **Controllers.** Every connected controller, numbered, with its owner. A light next to it turns green while you
  press anything on it. **Vibrate** buzzes the controller and blinks its light bar or player LED, so you can tell
  which one is which.
- **Settings.** Look speed, invert look, stick deadzone, menu cursor speed, rumble on damage, the join hint and the
  two-player layout.

Controllers also light up in their player's colour (light bar or player LED), and rumble when that player takes damage.

<details>
<summary><strong>Config file</strong></summary>

`config/couchcraft.json` is created on first launch. The settings screen edits the same values.

| Key | Default | Meaning |
| --- | --- | --- |
| `twoPlayerLayout` | `STACKED` | `STACKED` (P1 on top) or `SIDE_BY_SIDE` |
| `guestNames` | `Player2..4` | Only used once, to create the first profiles. Manage profiles in the settings screen. |
| `guestsInheritCheats` | `true` | Guests may use commands when the world allows them |
| `lookSpeedYaw` / `lookSpeedPitch` | `280` / `190` | Full-tilt turn speed, degrees per second |
| `invertLookY` | `false` | Invert vertical look |
| `stickDeadzone` | `0.16` | Stick deadzone |
| `cursorSpeed` | `300` | Menu cursor speed, GUI pixels per second |
| `rumble` | `true` | Rumble on damage |
| `showJoinHint` | `true` | "START to join" hint while a controller is free |
| `shoulderModifiers` | `true` | In menus, holding LB acts as Shift and RB as Ctrl |
| `showButtonMap` | `true` | Holding BACK draws a controller diagram of the buttons |

</details>

## Distant Horizons

Omega includes a compatibility layer for [Distant Horizons](https://modrinth.com/mod/distanthorizons) (tested with
3.3.4 on 26.3). It is applied only when Distant Horizons is installed. The mixins are not required, so if a future
Distant Horizons release changes the parts they target, the game logs the failure and runs without that fix.

What it does:

- **One LOD tree, every player.** LOD detail follows the nearest player, and Distant Horizons' background threads
  use the host's position, instead of re-centring on whichever view was swapped in last (which flickered).
- **Per-dimension lighting.** Each player's lightmap is filed under that player's own dimension.
- **Nether fog.** Vanilla Nether fog is removed (Distant Horizons does that everywhere), and Distant Horizons' own
  fog covers the LODs in the Nether instead, so the view fades out once. Two Distant Horizons bugs behind the
  invisible fog are fixed: the copy constructor of `DhApiFogRenderParam` caps the maximum thickness at the end
  fraction, and the fog colour came out pale grey-pink in the Nether.
- **No curtains in the Nether.** Dimensions with a ceiling get a larger vertical slice budget, so ceilings are not
  stretched down to the floor as dark vertical stripes.

Recommended `DistantHorizons.toml` settings: `pauseIfMovingQuickly=false`, `enableAntiAliasing=false`,
`reduceOverdrawWithFastMovement=false`, `useCameraPositionForQualityDropOff=false`, and a high
`horizontalQuality` (`EXTREME`) so detail does not drop with distance.

Settings are in `config/couch-dh-compat.properties`, created on first launch and read once at startup:

| Key | Default | Meaning |
| --- | --- | --- |
| `netherLodFogStartBlocks` | `-1` | Where fog on LODs starts in the Nether; negative means the vanilla chunk edge |
| `netherLodFogEndBlocks` | `400` | Where it is complete |
| `netherLodFogMin` | `0.0` | Minimum fog thickness; `1` is a diagnostic that makes LODs solid fog colour |
| `netherFarFadeEndBlocks` | `400` | LODs fade into the background colour by this distance (`0` disables) |
| `netherVerticalQuality` | `EXTREME` | Vertical detail for dimensions with a ceiling (`PIXEL_ART` is finer and slower) |
| `otherDimensionsLodFog` | `false` | Turn Distant Horizons' fog on for the overworld and End too |
| `debugLog` | `true` | Log fog values every few seconds in the Nether |

Known limits: the LOD tree is still centred on the host, so a guest farther than the LOD render radius from the
host gets no LODs, and a dimension that only guests are in is still centred on the host's position.

## Button map

Hold **BACK** while playing or in a menu. The buttons that have a label are outlined, the A/B/X/Y labels use their
button colours, and a button you are holding lights up. The picture shrinks to fit a split-screen view. The
sprite is generated from a character bitmap in `ButtonMap`.

Players can override labels in `config/couchcraft-button-map.properties`, for example `menu.A=select`; an empty
value hides a label (`world.RT=`). Inputs are `A B X Y LB RB LT RT L3 R3 BACK START DPAD_UP DPAD_DOWN DPAD_LEFT
DPAD_RIGHT LEFT_STICK RIGHT_STICK`, contexts are `menu` and `world`. Other mods can add labels from their client
initialiser:

```java
ButtonMap.describe(ButtonMap.Context.MENU, ButtonMap.Input.LB, () -> "hold: Shift");
ButtonMap.variable("mode", () -> MyMod.currentMode());
ButtonMap.describe(ButtonMap.Context.WORLD, ButtonMap.Input.DPAD_LEFT, () -> "mode: {mode}");
```

Suppliers run every frame for the live player. Returning `null` hides the label, or falls back to an earlier
registration.

## How it works

Minecraft's client is built around one player: one `player`, one `level`, one `GameRenderer`, one `Gui`.
Couchcraft gives every player a `PlayerContext` that holds their copy of all of those, and swaps it in for the short
moment that player is being ticked, drawn or sent a packet.

- **Network.** Each guest logs into your world's built-in server over its own in-memory connection, the same way
  the host does. The server sees ordinary players, which is why saving, permissions and advancements just work.
- **Ticks.** The game ticks once per player. Music, the sound engine and textures tick once.
- **Rendering.** Minecraft 26.3 renders in update → extract → render phases. Each runs once per player inside a
  *virtual window*: the game is told the window is that player's viewport, so the camera, GUI scale, menus and
  render targets size themselves. The finished frames are copied into one window-sized image with plain GPU copies.
- **Input.** Keyboard and mouse belong to P1, with the mouse remapped into P1's part of the screen. Controllers are
  read through SDL3's gamepad API, which Minecraft already ships, and drive each player's own key bindings, analog
  movement, camera and menu cursor.
- **A fix for vanilla.** 26.3's OpenGL backend passes a width where `glBlitFramebuffer` expects an end coordinate,
  so copying a texture to any spot except the top-left corner fails. Vanilla never does that; the compositor does,
  so Couchcraft corrects the call. Copies at the corner behave exactly as before.

## Build from source

You need JDK 25.

```sh
git clone https://github.com/Vasissualiyp/CouchcraftOmega.git
cd CouchcraftOmega
./gradlew build
```

The first build downloads the Distant Horizons jar (hash-checked, used only to compile the optional compat mixins; it is
not bundled). The jar ends up in `build/libs/`. To run a development client:

```sh
./gradlew runClient                  # dev client with the mod
./gradlew runClient -PcouchGuests=1  # adds one guest once you're in a world
./gradlew runClient -PcouchDev=Test  # automated test session (see below)
```

`-PcouchDev=<world>` creates or opens that world, plugs in virtual SDL controllers and plays a scripted session:
joining, walking, looking, menus, two-, three- and four-player layouts, death, the Nether, resizing, leaving, the host
quitting, rejoining, P1 on a controller, switching controllers and the settings screen. It writes captures to
`run/couchcraft-dev/`.

## Known limits

- Extra players can join singleplayer and LAN-host worlds, not online servers.
- Iris is not compatible, and Sodium is experimental (see above). Both replace the world renderer Couchcraft creates per player.
- Every extra player renders the world again, so four players cost roughly four times the GPU work of one.
- Guests can't type in chat or on signs, because there's only one keyboard.
- In small windows, half-screen views use a smaller GUI scale so the largest menus still fit.

## Contributing

Issues and pull requests are welcome. If something breaks, open an issue with your `latest.log`, your controller
model and how many players were in the game.

## License

[MIT](LICENSE), as the original (copyright (c) 2026 Zortos for Couchcraft; additions in Couchcraft Omega are by
Vasissualiyp under the same licence). Couchcraft Omega isn't affiliated with Mojang Studios or Microsoft. Minecraft is a trademark of Mojang
Synergies AB.
