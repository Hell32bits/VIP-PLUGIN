# Changelog

All notable changes to VipKeys are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Planned

- Optional key expiration (`expiresAt`) configurable per generated batch.
- `/vipkeys reload` command for configuration and kit reloads without a restart.
- Optional language file for customizing command and redemption messages.
- PlaceholderAPI support for displaying VIP status/expiry.

## 1.0.0 - 2026-11-06

### Added

- Initial public release for PowerNukkitX API 3.0.6 / Java 21.
- `/gerarchave <duration> <group> <amount> [kit]` to generate single-use VIP keys.
- `/chave <code>` to redeem keys and grant LuckPerms groups.
- Native LuckPerms node expiry for timed VIPs; `perm` for permanent groups.
- `/criarkitvip <kit> <group> [balance]` to capture the player's inventory as a VIP kit.
- `/editarkit <kit> <group> [balance]` to update an existing kit and re-capture items.
- `/listarkits` and `/apagarkit <kit>` for kit administration.
- Kit delivery on redemption, with overflow items dropped at the player's feet.
- LlamaEconomy balance rewards per kit.
- YAML storage for keys and kits (`chaves.yml` / `kits.yml`).
- MySQL storage for keys and kits using HikariCP, with optional table prefix.
- Tab completion for durations, LuckPerms groups and kit names.
- `vipkeys.admin` permission for administrative commands.
