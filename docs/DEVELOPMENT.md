# Build from source and test

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

## Benchmark

`./gradlew runClient -PcouchBench=<results.jsonl>` runs a scripted session and appends one JSON line per
measurement (frame-time percentiles per stage, load times). Options, passed as JVM properties:

| Property | Default | Meaning |
| --- | --- | --- |
| `-Dcouchcraft.benchPlayers=N` | `4` | Largest number of players to reach (1 to 4) |
| `-Dcouchcraft.benchDistance=N` | `16` | Render distance in chunks |

For each player count it measures the Overworld and the Nether, standing still and while the players fly through
ungenerated terrain. A view counts as loaded once every section around it has rendered and the chunks around the
player's new position have stayed stable for two seconds. Drop `sodium.jar` and `distanthorizons.jar` into `run/mods/`
to include them. Results from this harness are in [Benchmarks](BENCHMARKS.md).

[← Back to the README](../README.md)
