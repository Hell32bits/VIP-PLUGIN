# VipKeys

**Chaves de VIP resgatáveis para PowerNukkitX — grupos LuckPerms, kits de itens e recompensas de economia em um único comando.**

[English](README.md) · [Português (BR)](README.pt-BR.md)

![PowerNukkitX](https://img.shields.io/badge/PowerNukkitX-3.0.6-2ea44f)
![API](https://img.shields.io/badge/API-3.0.6-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Versão](https://img.shields.io/badge/vers%C3%A3o-1.0.0-lightgrey)

O VipKeys permite vender ou distribuir **acesso VIP** sem configuração manual. Gere um lote de
chaves, entregue os códigos aos jogadores e cada chave concede automaticamente um **grupo
LuckPerms**, entrega um **kit de itens personalizado** e pode creditar **balance no LlamaEconomy**
no momento do resgate. As chaves são de uso único e cada resgate fica registrado.

---

## Índice

- [Funcionalidades](#funcionalidades)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Início rápido](#início-rápido)
- [Comandos](#comandos)
- [Permissões](#permissões)
- [Configuração](#configuração)
- [Armazenamento](#armazenamento)
- [Como o resgate funciona](#como-o-resgate-funciona)
- [Kits e economia](#kits-e-economia)
- [Solução de problemas](#solução-de-problemas)
- [Licença](#licença)

---

## Funcionalidades

- **Geração de chaves em lote** — chaves criptograficamente aleatórias (`SecureRandom`, `A-Z0-9`,
  8–32 caracteres, padrão 12), persistidas imediatamente.
- **Resgate único** — cada chave só pode ser resgatada uma vez; o plugin registra quem usou e quando.
- **Integração com LuckPerms** — concede o grupo configurado no resgate. VIPs temporários usam o
  `expiry` nativo do LuckPerms, então o grupo expira **mesmo após reiniciar o servidor**.
- **Kits VIP** — capture seu inventário com `/criarkitvip` e entregue os itens exatos (incluindo NBT:
  encantamentos, nomes customizados, lore) no resgate. Itens que não couberem são dropados aos pés
  do jogador.
- **Recompensas de economia** — opcionalmente credite balance via LlamaEconomy junto com o kit.
- **Armazenamento YAML ou MySQL** — arquivo simples por padrão, ou MySQL (HikariCP) para redes.
- **Ferramentas administrativas** — criar, editar, listar e apagar kits; tab completion para
  durações, grupos do LuckPerms e nomes de kits.
- **Degradação graciosa** — o plugin inicia mesmo sem LuckPerms ou LlamaEconomy, e o resgate informa
  um erro claro em vez de falhar silenciosamente.

## Requisitos

| Requisito | Versão | Observações |
|---|---|---|
| **PowerNukkitX** | API **3.0.6** | O PNX exige Java 21. |
| **Java** | **21+** | Mesma JVM usada pelo servidor. |
| **LuckPerms** | 5.x | Necessário para `/chave` (concessão de grupo). |
| **LlamaEconomy** | 1.0.x | Necessário apenas quando um kit tem `balance > 0`. |
| **MySQL** | 5.7+ / 8.x | Opcional. YAML é o padrão. |
| **HikariCP** | 5.x | Já incluído no PNX; necessário apenas para MySQL. |

> LuckPerms e LlamaEconomy são **dependências opcionais** (`softdepend`). Você pode instalar o
> VipKeys primeiro e adicioná-los depois, mas o resgate exige LuckPerms.

## Instalação

### A partir de uma release

1. Baixe o `VipKeys.jar` mais recente na página de **Releases** do repositório.
2. Coloque-o na pasta `plugins/` do servidor.
3. Instale o **LuckPerms** (obrigatório) e o **LlamaEconomy** (apenas se usar balance nos kits).
4. Reinicie o servidor.
5. Edite `plugins/VipKeys/config.yml` se necessário e reinicie novamente.

Na primeira inicialização o plugin cria:

```text
plugins/VipKeys/
├── config.yml     # configurações do plugin
├── chaves.yml     # chaves geradas   (armazenamento YAML)
└── kits.yml       # kits VIP         (armazenamento YAML)
```

## Início rápido

```text
# 1. Crie o grupo do LuckPerms que a chave vai conceder
/lp creategroup vip_gold

# 2. Coloque os itens do kit no inventário e capture-os
/criarkitvip vip_gold vip_gold 250

# 3. Gere 10 chaves de 30 dias com o kit associado
/gerarchave 30d vip_gold 10 vip_gold

# 4. Entregue um código ao jogador; ele resgata com
/chave A1B2C3D4E5F6
```

Em caso de sucesso, o jogador recebe:

- o grupo `vip_gold` por **30 dias** (removido automaticamente pelo LuckPerms),
- todos os itens armazenados no kit `vip_gold`,
- **250** de balance no LlamaEconomy.

## Comandos

Todos os comandos são registrados pelo plugin na inicialização. Os nomes não diferenciam
maiúsculas de minúsculas.

| Comando | Quem pode usar | Permissão | Descrição |
|---|---|---|---|
| `/gerarchave <duração> <grupo> <quantidade> [kit]` | Jogador ou console | `vipkeys.admin` | Gera `quantidade` chaves para um grupo LuckPerms, opcionalmente com kit. |
| `/chave <código>` | Apenas jogador | — | Resgata uma chave para o jogador que executou. |
| `/criarkitvip <kit> <grupo> [balance]` | Apenas jogador | `vipkeys.admin` | Captura o inventário do jogador como um kit. `balance` padrão: `0`. |
| `/editarkit <kit> <grupo> [balance]` | Apenas jogador | `vipkeys.admin` | Recaptura o inventário e atualiza grupo/balance. Omitir `balance` mantém o valor atual. |
| `/listarkits` | Jogador ou console | `vipkeys.admin` | Lista todos os kits com grupo, balance e quantidade de itens. |
| `/apagarkit <kit>` | Jogador ou console | `vipkeys.admin` | Apaga um kit. Chaves que o referenciam falharão com “kit não encontrado”. |

Referência detalhada: [`docs/COMMANDS.md`](docs/COMMANDS.md).

### `/gerarchave`

```text
/gerarchave <duração> <grupo> <quantidade> [kit]
```

| Argumento | Obrigatório | Descrição |
|---|---|---|
| `duração` | sim | `30s`, `15m`, `12h`, `7d`, `30d`, `perm` (também `permanent`/`permanente`, `day(s)`, `hour(s)`, `minute(s)`, `second(s)`). |
| `grupo` | sim | Grupo LuckPerms concedido no resgate. Precisa existir quando o LuckPerms está disponível. |
| `quantidade` | sim | Número de chaves, entre `1` e `keys.maxQuantity` (padrão `100`). |
| `kit` | não | Nome do kit criado com `/criarkitvip`. Precisa existir. |

Exemplos:

```text
/gerarchave 7d vip 5          # 5 chaves de 7 dias para o grupo "vip", sem kit
/gerarchave 30d vip_gold 10 vip_gold
/gerarchave perm super 1      # 1 chave permanente para o grupo "super"
```

### `/chave`

```text
/chave <código>
```

Resgata uma chave para o jogador que executou o comando. Os códigos **não diferenciam maiúsculas de
minúsculas** (`a1b2...` e `A1B2...` são a mesma chave). Apenas jogadores podem resgatar chaves; o
console é rejeitado.

### Comandos de kit

```text
/criarkitvip vip_gold vip_gold 250
/editarkit  vip_gold vip_gold 500
/listarkits
/apagarkit  vip_gold
```

Nomes de kit devem seguir `^[a-z0-9_]+$` (letras minúsculas, números e underscore). Letras
maiúsculas são rejeitadas; use a forma minúscula em todos os comandos de kit.

> **Sobre o grupo do kit:** o grupo passado em `/criarkitvip` é armazenado no kit e exibido pelo
> `/listarkits`. O grupo realmente concedido no resgate é o que está **na chave**, definido pelo
> `/gerarchave`. Use o mesmo grupo nos dois comandos, a menos que queira essa separação de propósito.

## Permissões

| Permissão | Padrão | Descrição |
|---|---|---|
| `vipkeys.admin` | `op` | Permite `/gerarchave`, `/criarkitvip`, `/editarkit`, `/listarkits` e `/apagarkit`. |

O `/chave` não tem nó de permissão: qualquer jogador pode resgatar uma chave válida. Para restringir,
use um nó de permissão próprio ou configure o comando no seu plugin de permissões.

## Configuração

O `plugins/VipKeys/config.yml` padrão:

```yaml
storage:
  # "yml" (padrão) ou "mysql"
  type: yml

  yml:
    # Arquivo de chaves, relativo a plugins/VipKeys/
    file: chaves.yml
    # Arquivo de kits, relativo a plugins/VipKeys/
    kitFile: kits.yml

  mysql:
    host: localhost
    port: 3306
    database: vipkeys
    username: root
    password: ""
    # Prefixo opcional. "vip_" -> "vip_vip_keys" / "vip_vip_kits".
    tablePrefix: ""

keys:
  # Comprimento da chave (limitado a 8..32). Padrão: 12.
  length: 12
  # Máximo de chaves por /gerarchave (limitado a 1..10000). Padrão: 100.
  maxQuantity: 100
```

> Reinicie o servidor após alterar o armazenamento. Mudar `storage.type` **não** migra chaves/kits
> entre YAML e MySQL; migre os dados manualmente se necessário.

Referência detalhada: [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

## Armazenamento

### YAML (padrão)

As chaves ficam em `chaves.yml`:

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

Os kits ficam em `kits.yml`:

```yaml
kits:
  vip_gold:
    group: vip_gold
    balance: 250.0
    items:
      - "minecraft:diamond|0|64|"
      - "minecraft:diamond_sword|0|1|BASE64_NBT_AQUI"
```

Formato de item: `id|meta|count|base64NBT`. A parte NBT fica vazia quando o item não tem NBT. Faça
backup dos dois arquivos antes de atualizar o plugin ou mudar o armazenamento.

### MySQL

Com `storage.type: mysql`, o VipKeys cria duas tabelas automaticamente:

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

Com `tablePrefix: "vip_"` os nomes viram `vip_vip_keys` e `vip_vip_kits`. O plugin usa HikariCP para
pool de conexões; o PNX já inclui o HikariCP.

## Como o resgate funciona

O `/chave <código>` faz estas verificações **antes** de alterar qualquer coisa:

1. A chave existe (sem diferenciar maiúsculas/minúsculas).
2. A chave ainda não foi usada.
3. A própria chave não expirou (chaves geradas por esta versão não expiram; é uma proteção para
   dados editados manualmente).
4. O LuckPerms está disponível.
5. O grupo LuckPerms da chave existe.
6. Se a chave referencia um kit, o kit existe.
7. Se o kit tem `balance > 0`, o LlamaEconomy está disponível.

Depois da validação:

1. O grupo é adicionado ao jogador, com `expiry` do LuckPerms quando a duração não é permanente.
2. Os itens do kit são clonados e adicionados ao inventário; o excedente é dropado aos pés do jogador.
3. O balance é creditado via LlamaEconomy.
4. A chave é marcada como usada (`used`, `usedBy`, `usedAt`) e persistida.

Resultados exibidos ao jogador:

| Resultado | Mensagem |
|---|---|
| `INVALID` | Chave inválida. |
| `ALREADY_USED` | Chave já usada. |
| `EXPIRED` | Chave expirada. |
| `NO_LUCKPERMS` | LuckPerms não está disponível no servidor. Contate um administrador. |
| `NO_ECONOMY` | LlamaEconomy não está disponível para creditar o balance deste kit. |
| `GROUP_NOT_FOUND` | Grupo não encontrado no LuckPerms. |
| `KIT_NOT_FOUND` | Kit associado à chave não encontrado. |
| `ERROR` | Ocorreu um erro ao resgatar a chave. |

> **As chaves não expiram sozinhas.** Uma chave gerada continua válida até ser resgatada. A duração
> configurada controla por quanto tempo o **grupo** dura após o resgate, não a validade do código.
> Se precisar de códigos com validade, remova chaves não usadas do armazenamento ou estenda o modelo.

## Kits e economia

- O `/criarkitvip` copia todos os itens não vazios do inventário do jogador, incluindo NBT
  (encantamentos, nomes customizados, lore etc.).
- O inventário do jogador **não** é limpo; o kit é uma cópia, não uma retirada.
- `balance` é opcional e o padrão é `0`. Um kit com `balance: 0` não exige LlamaEconomy.
- Se o inventário estiver cheio no resgate, os itens restantes são dropados na localização do jogador.
- Apagar um kit não apaga as chaves que o referenciam. Resgatar uma dessas chaves retorna
  `KIT_NOT_FOUND`; recrie o kit com o mesmo nome ou gere novas chaves.

## Solução de problemas

| Sintoma | Causa / solução |
|---|---|
| “LuckPerms não está disponível…” | Instale/ative o LuckPerms. Verifique no log `LuckPerms encontrado`. |
| “Grupo não encontrado no LuckPerms.” | Crie o grupo primeiro (`/lp creategroup <grupo>`) ou corrija o nome na chave. |
| “LlamaEconomy não está disponível…” | Instale/ative o LlamaEconomy ou defina o balance do kit como `0`. |
| “Kit associado à chave não encontrado.” | O kit foi apagado ou renomeado. Recrie-o ou gere novas chaves. |
| Itens sumiram / foram para o chão | O inventário estava cheio; o excedente é dropado aos pés do jogador. |
| Chaves não são salvas | Verifique permissões de escrita em `plugins/VipKeys/` (YAML) ou credenciais/conexão do MySQL. |
| Erros de conexão MySQL | Confira host, porta, banco, usuário, senha e se o banco existe. |
| Tab completion não lista grupos | LuckPerms indisponível ou falha ao carregar grupos; veja o console. |
| O plugin não inicia | Procure `Falha ao habilitar o VipKeys` e o stack trace no console. |
