# Beacon Range Extender

[![Modrinth](https://img.shields.io/modrinth/dt/AaBVswQ1?label=Modrinth&logo=modrinth)](https://modrinth.com/project/beacon-range-extender)

A Minecraft Fabric mod, which extends the range of beacons.

## Configuration

Within the `config` directory, there is `beacon-range-extender.json`.

It has the following options:

- `rangePerLevel`: the base range each level provides
- `baseRange`: the range the beacon gives as long as it's activated
- `rangeMultipliers`: if a level is made of only one material, the range can (and by default will) be multiplied by the provided value.
  - By default, diamond blocks double the range and netherite blocks x4 it.
  - This is given as `[block identifier]: [multiplier]`

Note: The range is calculated with `(level * rangePerLevel * rangeMultiplier) + baseRange` (adapted from vanilla: `(level * 10) + 10`)
