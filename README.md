# VipKeys

**Redeemable VIP keys for PowerNukkitX — LuckPerms groups, item kits and economy rewards in one command.**

[English](README.md) · [Português (BR)](README.pt-BR.md)

![PowerNukkitX](https://img.shields.io/badge/PowerNukkitX-3.0.6-2ea44f)
![API](https://img.shields.io/badge/API-3.0.6-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Version](https://img.shields.io/badge/version-1.0.0-lightgrey)

VipKeys lets server owners sell or give away **VIP access** without manual setup. Generate a batch of
keys, hand them to players, and each key automatically grants a **LuckPerms group**, delivers a
**custom item kit** and can credit **LlamaEconomy balance** on redemption. Keys are single-use and
every redemption is recorded.

---

## Table of contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [Storage](#storage)
- [How redemption works](#how-redemption-works)
- [Kits and economy](#kits-and-economy)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

- **Batch key generation** — cryptographically random keys (`SecureRandom`, `A-Z0-9`, 8–32 chars,
  default 12), persisted immediately.
- **One-time redemption** — a key can only be redeemed once; the plugin records who used it and when.
- **LuckPerms integration** — grants the configured group on redemption. Timed VIPs use LuckPerms'
  native node expiry, so the group expires **even after a server restart**.
- **VIP kits** — capture your inventory with `/criarkitvip` and deliver the exact items (including
  NBT: enchantments, custom names, lore) on redemption. Items that do not fit are dropped at the
  player's feet.
- **Economy rewards** — optionally credit LlamaEconomy balance together with the kit.
- **YAML or MySQL storage** — simple file storage by default, or MySQL (HikariCP) for networks.
- **Admin tooling** — create, edit, list and delete kits; tab completion for durations, LuckPerms
  groups and kit names.
- **Graceful degradation** — the plugin still starts if LuckPerms or LlamaEconomy is missing, and
  redemption reports a clear error instead of failing silently.

## Requirements

| Requirement | Version | Notes |
|---|---|---|
| **PowerNukkitX** | API **3.0.6** | Java 21 is required by PNX. |
| **Java** | **21+** | Same JVM used by the server. |
| **LuckPerms** | 5.x | Required for `/chave` (group grant). |
| **LlamaEconomy** | 1.0.x | Required only when a kit has `balance > 0`. |
| **MySQL** | 5.7+ / 8.x | Optional. YAML is the default. |
| **HikariCP** | 5.x | Already bundled with PNX; only needed for MySQL. |

> LuckPerms and LlamaEconomy are declared as **soft dependencies**. You can install VipKeys first
> and add them later, but redemption requires LuckPerms.

## Installation

### From a release

1. Download the latest `VipKeys.jar` from the repository's **Releases** page.
2. Place it in your server's `plugins/` directory.
3. Install **LuckPerms** (required) and **LlamaEconomy** (only if you use balance rewards).
4. Restart the server.
5. Edit `plugins/VipKeys/config.yml` if needed and restart again.

On first start the plugin creates:

```text
plugins/VipKeys/
├── config.yml     # plugin settings
├── chaves.yml     # generated keys   (YAML storage)
└── kits.yml       # VIP kits         (YAML storage)
```

## Quick start

```text
# 1. Create the LuckPerms group that the key will grant
/lp creategroup vip_gold

# 2. Put the kit items in your inventory, then capture them
/criarkitvip vip_gold vip_gold 250

# 3. Generate 10 keys valid for 30 days, with the kit attached
/gerarchave 30d vip_gold 10 vip_gold

# 4. Give a code to a player; they redeem it with
/chave A1B2C3D4E5F6
```

On success the player receives:

- the `vip_gold` group for **30 days** (removed automatically by LuckPerms),
- every item stored in the `vip_gold` kit,
- **250** in LlamaEconomy balance.

## Commands

All commands are registered by the plugin at startup. Command names are case-insensitive.

| Command | Who can run | Permission | Description |
|---|---|---|---|
| `/gerarchave <duration> <group> <amount> [kit]` | Player or console | `vipkeys.admin` | Generates `amount` keys for a LuckPerms group, optionally attached to a kit. |
| `/chave <code>` | Player only | — | Redeems a key for the executing player. |
| `/criarkitvip <kit> <group> [balance]` | Player only | `vipkeys.admin` | Captures the player's inventory as a kit. `balance` defaults to `0`. |
| `/editarkit <kit> <group> [balance]` | Player only | `vipkeys.admin` | Re-captures the inventory and updates group/balance. Omit `balance` to keep the current value. |
| `/listarkits` | Player or console | `vipkeys.admin` | Lists every kit with group, balance and item count. |
| `/apagarkit <kit>` | Player or console | `vipkeys.admin` | Deletes a kit. Keys that reference it will fail with “kit not found”. |

Detailed reference: [`docs/COMMANDS.md`](docs/COMMANDS.md).

### `/gerarchave`

```text
/gerarchave <duration> <group> <amount> [kit]
```

| Argument | Required | Description |
|---|---|---|
| `duration` | yes | `30s`, `15m`, `12h`, `7d`, `30d`, `perm` (also `permanent`/`permanente`, `day(s)`, `hour(s)`, `minute(s)`, `second(s)`). |
| `group` | yes | LuckPerms group granted on redemption. Must exist when LuckPerms is available. |
| `amount` | yes | Number of keys, between `1` and `keys.maxQuantity` (default `100`). |
| `kit` | no | Kit name created with `/criarkitvip`. Must exist. |

Examples:

```text
/gerarchave 7d vip 5          # 5 keys, 7 days of "vip", no kit
/gerarchave 30d vip_gold 10 vip_gold
/gerarchave perm super 1      # 1 permanent key for "super"
```

### `/chave`

```text
/chave <code>
```

Redeems a key for the player who ran the command. Codes are **case-insensitive** (`a1b2...` and
`A1B2...` are the same key). Only players can redeem keys; console is rejected.

### Kit commands

```text
/criarkitvip vip_gold vip_gold 250
/editarkit  vip_gold vip_gold 500
/listarkits
/apagarkit  vip_gold
```

Kit names must match `^[a-z0-9_]+$` (lowercase letters, digits and underscore). Uppercase
letters are rejected; use the lowercase form in every kit command.

> **About the kit group:** the group passed to `/criarkitvip` is stored with the kit and shown by
> `/listarkits`. The group actually granted on redemption is the one stored **on the key** by
> `/gerarchave`. Use the same group in both commands unless you intentionally want different
> bookkeeping.

## Permissions

| Permission | Default | Description |
|---|---|---|
| `vipkeys.admin` | `op` | Allows `/gerarchave`, `/criarkitvip`, `/editarkit`, `/listarkits` and `/apagarkit`. |

`/chave` has no permission node: every player can redeem a valid key. To restrict it, grant a
permission plugin node or wrap the command in your permissions setup.

## Configuration

The default `plugins/VipKeys/config.yml`:

```yaml
storage:
  # "yml" (default) or "mysql"
  type: yml

  yml:
    # Key file, relative to plugins/VipKeys/
    file: chaves.yml
    # Kit file, relative to plugins/VipKeys/
    kitFile: kits.yml

  mysql:
    host: localhost
    port: 3306
    database: vipkeys
    username: root
    password: ""
    # Optional table prefix. "vip_" -> "vip_vip_keys" / "vip_vip_kits".
    tablePrefix: ""

keys:
  # Key length (clamped to 8..32). Default: 12.
  length: 12
  # Maximum keys per /gerarchave (clamped to 1..10000). Default: 100.
  maxQuantity: 100
```

> Restart the server after changing storage settings. Changing `storage.type` does **not** migrate
> existing keys/kits between YAML and MySQL; migrate the data manually if needed.

Detailed reference: [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

## Storage

### YAML (default)

Keys are stored in `chaves.yml`:

```yaml
keys:
  A1B2C3D4E5F6:
    group: vip_gold
    duration: 30d
    durationMs: 2592000000
    createdAt: 1730000000000
    expiresAt: 0
    used: false
    usedBy: ""
    usedAt: 0
    kitName: vip_gold
```

Kits are stored in `kits.yml`:

```yaml
kits:
  vip_gold:
    group: vip_gold
    balance: 250.0
    items:
      - "minecraft:diamond|0|64|"
      - "minecraft:diamond_sword|0|1|BASE64_NBT_HERE"
```

Item format: `id|meta|count|base64NBT`. The NBT part is empty when the item has no NBT. Back up
both files before upgrading or changing storage.

### MySQL

When `storage.type: mysql`, VipKeys creates two tables automatically:

```sql
CREATE TABLE IF NOT EXISTS `vip_keys` (
  `code`        VARCHAR(12) NOT NULL,
  `group_name`  VARCHAR(64) NOT NULL,
  `duration`    VARCHAR(32) NOT NULL,
  `duration_ms` BIGINT      NOT NULL,
  `created_at`  BIGINT      NOT NULL,
  `expires_at`  BIGINT      NOT NULL DEFAULT 0,
  `used`        TINYINT(1)  NOT NULL DEFAULT 0,
  `used_by`     VARCHAR(64) NULL,
  `used_at`     BIGINT      NOT NULL DEFAULT 0,
  `kit_name`    VARCHAR(64) NULL,
  PRIMARY KEY (`code`)
);

CREATE TABLE IF NOT EXISTS `vip_kits` (
  `name`       VARCHAR(64) NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  `balance`    DOUBLE      NOT NULL DEFAULT 0,
  `items`      TEXT        NULL,
  PRIMARY KEY (`name`)
);
```

With `tablePrefix: "vip_"` the names become `vip_vip_keys` and `vip_vip_kits`. The plugin uses
HikariCP for connection pooling; PNX already ships HikariCP.

## How redemption works

`/chave <code>` performs these checks **before** changing anything:

1. The key exists (case-insensitive).
2. The key has not been used.
3. The key itself has not expired (keys generated by this version do not expire; this is a safety
   check for hand-edited data).
4. LuckPerms is available.
5. The key's LuckPerms group exists.
6. If the key references a kit, the kit exists.
7. If the kit has `balance > 0`, LlamaEconomy is available.

After validation:

1. The group is added to the player, with a LuckPerms `expiry` when the duration is not permanent.
2. The kit items are cloned and added to the inventory; overflow is dropped at the player's feet.
3. The balance is credited through LlamaEconomy.
4. The key is marked as used (`used`, `usedBy`, `usedAt`) and persisted.

Failure results shown to the player:

| Result | Message |
|---|---|
| `INVALID` | Chave inválida. |
| `ALREADY_USED` | Chave já usada. |
| `EXPIRED` | Chave expirada. |
| `NO_LUCKPERMS` | LuckPerms não está disponível no servidor. Contate um administrador. |
| `NO_ECONOMY` | LlamaEconomy não está disponível para creditar o balance deste kit. |
| `GROUP_NOT_FOUND` | Grupo não encontrado no LuckPerms. |
| `KIT_NOT_FOUND` | Kit associado à chave não encontrado. |
| `ERROR` | Ocorreu um erro ao resgatar a chave. |

> The plugin's user-facing messages are currently in Brazilian Portuguese. The table above shows the
> exact strings players will see.

> **Keys do not expire by themselves.** A generated key stays valid until redeemed. The configured
> duration controls how long the **group** lasts after redemption, not how long the code is valid.
> If you need expiring codes, delete unused keys from storage or extend the model.

## Kits and economy

- `/criarkitvip` copies every non-air item from the executing player's inventory, including NBT
  (enchantments, custom names, lore, etc.).
- The player's inventory is **not** cleared; the kit is a snapshot, not a withdrawal.
- `balance` is optional and defaults to `0`. A kit with `balance: 0` does not require LlamaEconomy.
- If the inventory is full on redemption, remaining items are dropped at the player's location.
- Deleting a kit does not delete keys that reference it. Redeeming one of those keys returns
  `KIT_NOT_FOUND`; recreate the kit with the same name or generate new keys.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| “LuckPerms não está disponível…” | Install/enable LuckPerms. Check the startup log for `LuckPerms encontrado`. |
| “Grupo não encontrado no LuckPerms.” | Create the group first (`/lp creategroup <group>`) or fix the name in the key. |
| “LlamaEconomy não está disponível…” | Install/enable LlamaEconomy, or set the kit balance to `0`. |
| “Kit associado à chave não encontrado.” | The kit was deleted or renamed. Recreate it or generate new keys. |
| Items disappeared / went on the ground | The inventory was full; overflow is dropped at the player's feet. |
| Keys are not saved | Check write permissions on `plugins/VipKeys/` (YAML) or MySQL credentials/connectivity. |
| MySQL connection errors | Verify host, port, database, user, password and that the database exists. |
| Tab completion does not list groups | LuckPerms is unavailable or groups failed to load; check the console. |
| Plugin does not start | Check the console for `Falha ao habilitar o VipKeys` and the stack trace. |

## License

This project does not currently include a license file. Contact the project maintainer for licensing
terms.
