# Distant Horizons

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
| `debugLog` | `false` | Log fog values every few seconds in the Nether |

Known limits: the LOD tree is still centred on the host, so a guest farther than the LOD render radius from the
host gets no LODs, and a dimension that only guests are in is still centred on the host's position.

[← Back to the README](../README.md)
