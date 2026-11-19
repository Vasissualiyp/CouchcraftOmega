# Changelog

## 0.2.1

- New icon: a capital Omega coloured by player quadrant, replacing the original Couchcraft mark.
- The Distant Horizons fog log (`debugLog` in `couch-dh-compat.properties`) is off by default. Existing config files
  keep their value; set it to `false` to stop the log line every few seconds in the Nether.
- The mod's homepage link points to this repository.

## 0.2.0

- Benchmarks for Sodium and Distant Horizons with 1 to 4 players ([docs/BENCHMARKS.md](docs/BENCHMARKS.md)).
- README split into topic pages under `docs/`.

## 0.1.1

- Fix: `provides` in `fabric.mod.json` must be a plain string (0.1.0 failed to load in Prism).

## 0.1.0

First release of Couchcraft Omega, an enhanced fork of Couchcraft 0.2.0:

- Distant Horizons 3.3.4 support in split screen.
- Shift and Ctrl on LB and RB in menus.
- Controller button map (hold BACK) with an API for other mods.
- Sodium allowed (experimental); Iris still blocked.
