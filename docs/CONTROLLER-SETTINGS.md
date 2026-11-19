# Controller settings

Open them from the pause menu (**Couch Co-op…**) or with <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>.

<p align="center">
  <img src="assets/readme/settings.jpg" alt="The Couch Co-op settings screen open in Player 1's half of the screen, listing each player with their controller and a Vibrate button." width="100%" />
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

[← Back to the README](../README.md)
