# Referência de configuração

Arquivo: `plugins/VipKeys/config.yml`

O plugin copia o `config.yml` padrão na primeira inicialização e lê as opções em `onEnable`.
**Não há comando de reload**; reinicie o servidor após alterar o arquivo.

## Estrutura completa

```yaml
storage:
  type: yml

  yml:
    file: chaves.yml
    kitFile: kits.yml

  mysql:
    host: localhost
    port: 3306
    database: vipkeys
    username: root
    password: ""
    tablePrefix: ""

keys:
  length: 12
  maxQuantity: 100
```

## `storage`

| Chave | Tipo | Padrão | Descrição |
|---|---|---|---|
| `storage.type` | texto | `yml` | `yml` (arquivo) ou `mysql`. Qualquer outro valor cai no YAML. |

### `storage.yml`

| Chave | Tipo | Padrão | Descrição |
|---|---|---|---|
| `storage.yml.file` | texto | `chaves.yml` | Arquivo de chaves, relativo a `plugins/VipKeys/`. |
| `storage.yml.kitFile` | texto | `kits.yml` | Arquivo de kits, relativo a `plugins/VipKeys/`. |

Os caminhos são resolvidos com `new File(plugin.getDataFolder(), valor)`. Subpastas são criadas se
não existirem (ex.: `data/chaves.yml`).

### `storage.mysql`

| Chave | Tipo | Padrão | Descrição |
|---|---|---|---|
| `storage.mysql.host` | texto | `localhost` | Host do MySQL. |
| `storage.mysql.port` | inteiro | `3306` | Porta. |
| `storage.mysql.database` | texto | `vipkeys` | Nome do banco (precisa existir). |
| `storage.mysql.username` | texto | `root` | Usuário. |
| `storage.mysql.password` | texto | vazio | Senha. |
| `storage.mysql.tablePrefix` | texto | vazio | Prefixo das tabelas. |

Com `tablePrefix: "vip_"`, as tabelas são `vip_vip_keys` e `vip_vip_kits`.

O plugin usa HikariCP com as seguintes configurações fixas:

| Armazenamento | Pool | Máx. conexões | Mín. idle | Timeout |
|---|---|---|---|---|
| Chaves | `VipKeysPool` | 10 | 2 | 10 s |
| Kits | `VipKeysKitPool` | 5 | 1 | 10 s |

> O banco precisa existir; o plugin cria as **tabelas**, mas não o database. As credenciais ficam em
> texto puro no `config.yml`; restrinja as permissões do arquivo no sistema operacional.

## `keys`

| Chave | Tipo | Padrão | Limites | Descrição |
|---|---|---|---|---|
| `keys.length` | inteiro | `12` | `8..32` | Comprimento dos códigos gerados. |
| `keys.maxQuantity` | inteiro | `100` | `1..10000` | Máximo de chaves por `/gerarchave`. |

Valores fora do intervalo são ajustados (`clamp`) na inicialização.

O alfabeto das chaves é `A-Z0-9`, gerado com `SecureRandom`. O comprimento não altera chaves já
geradas.

## Armazenamento YAML

### `chaves.yml`

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

| Campo | Tipo | Descrição |
|---|---|---|
| `group` | texto | Grupo LuckPerms concedido. |
| `duration` | texto | Duração original (`7d`, `perm`, etc.). |
| `durationMs` | inteiro | Duração em ms; `-1` = permanente. |
| `createdAt` | inteiro | Epoch ms da criação. |
| `expiresAt` | inteiro | Expiração da chave; `0` = nunca. As chaves geradas usam `0`. |
| `used` | booleano | Se já foi resgatada. |
| `usedBy` | texto | UUID de quem resgatou; vazio quando não usada. |
| `usedAt` | inteiro | Epoch ms do resgate; `0` quando não usada. |
| `kitName` | texto | Kit associado; vazio = sem kit. |

### `kits.yml`

```yaml
kits:
  vip_gold:
    group: vip_gold
    balance: 250.0
    items:
      - "minecraft:diamond|0|64|"
      - "minecraft:diamond_sword|0|1|BASE64_NBT"
```

| Campo | Tipo | Descrição |
|---|---|---|
| `group` | texto | Grupo associado ao kit (armazenado/exibido). |
| `balance` | decimal | Balance creditado no resgate; `0` = sem economia. |
| `items` | lista de textos | Itens serializados. |

## Armazenamento MySQL

### `vip_keys`

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
```

### `vip_kits`

```sql
CREATE TABLE IF NOT EXISTS `vip_kits` (
  `name`       VARCHAR(64) NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  `balance`    DOUBLE      NOT NULL DEFAULT 0,
  `items`      TEXT        NULL,
  PRIMARY KEY (`name`)
);
```

As operações usam `REPLACE INTO` (upsert por chave primária) para chaves e kits.

## Serialização de itens

Cada item é armazenado como uma linha:

```text
id|meta|count|base64NBT
```

| Parte | Exemplo | Descrição |
|---|---|---|
| `id` | `minecraft:diamond` | Identificador do item. |
| `meta` | `0` | Damage/metadata. |
| `count` | `64` | Quantidade. |
| `base64NBT` | vazio ou Base64 | NBT serializado (encantamentos, nome, lore etc.). |

No YAML, cada item é uma entrada da lista `items`. No MySQL, os itens são unidos por `\n` na coluna
`items` (`TEXT`).

> O formato é específico do PNX. Não edite manualmente o Base64 a menos que saiba o que está fazendo.

## Migração e backup

- Mudar `storage.type` **não** migra dados. O plugin começa a ler do novo armazenamento vazio.
- Para migrar YAML → MySQL, pare o servidor, importe os registros manualmente (respeitando as
  colunas acima) e então altere `storage.type`.
- Faça backup de `plugins/VipKeys/` (YAML) ou um dump do banco (MySQL) antes de atualizar o plugin.
- Chaves já resgatadas não podem ser “resetadas” pelo comando; edite o armazenamento com cuidado ou
  gere novas chaves.

## Segurança

- Use um usuário MySQL dedicado, com permissão apenas no banco do plugin.
- Proteja `config.yml` (credenciais em texto puro).
- Não compartilhe `chaves.yml`/`kits.yml`: eles contêm códigos válidos e dados de itens.
- Códigos de chave são sensíveis; trate como senhas até o resgate.
