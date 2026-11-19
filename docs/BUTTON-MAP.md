# Button map

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

[← Back to the README](../README.md)
