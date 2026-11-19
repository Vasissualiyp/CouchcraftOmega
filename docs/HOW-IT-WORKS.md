# How it works

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

[← Back to the README](../README.md)
