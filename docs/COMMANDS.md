# Referência de comandos

Todos os comandos são registrados manualmente por `VipKeysPlugin` na inicialização
(`getServer().getCommandMap().register(...)`). Não há aliases.

- Nomes de comandos não diferenciam maiúsculas de minúsculas.
- `vipkeys.admin` é verificada no código; operadores (`op`) também podem usar os comandos
  administrativos.
- O console pode executar comandos administrativos, exceto os que capturam inventário
  (`/criarkitvip` e `/editarkit`).

## Resumo

| Comando | Executor | Permissão | Captura inventário |
|---|---|---|---|
| `/gerarchave <duração> <grupo> <quantidade> [kit]` | Jogador ou console | `vipkeys.admin` | Não |
| `/chave <código>` | Jogador | — | Não |
| `/criarkitvip <kit> <grupo> [balance]` | Jogador | `vipkeys.admin` | Sim |
| `/editarkit <kit> <grupo> [balance]` | Jogador | `vipkeys.admin` | Sim |
| `/listarkits` | Jogador ou console | `vipkeys.admin` | Não |
| `/apagarkit <kit>` | Jogador ou console | `vipkeys.admin` | Não |

---

## `/gerarchave`

Gera um lote de chaves de uso único para um grupo LuckPerms, opcionalmente associadas a um kit.

```text
/gerarchave <duração> <grupo> <quantidade> [kit]
```

### Argumentos

| Argumento | Tipo | Obrigatório | Regras |
|---|---|---|---|
| `duração` | texto | Sim | `Ns`, `Nm`, `Nh`, `Nd` ou `perm`. Veja [durações](#durações). |
| `grupo` | texto | Sim | Grupo do LuckPerms. Se o LuckPerms estiver disponível, o grupo precisa existir. |
| `quantidade` | inteiro | Sim | Entre `1` e `keys.maxQuantity` (padrão `100`, limite máximo `10000`). |
| `kit` | texto | Não | Nome de um kit existente. Se não existir, o comando falha. |

### Durações

O parser aceita, sem diferenciar maiúsculas/minúsculas:

| Entrada | Significado |
|---|---|
| `perm`, `permanent`, `permanente` | VIP permanente (sem expiração) |
| `30s`, `30sec`, `30second`, `30seconds` | segundos |
| `15m`, `15min`, `15minute`, `15minutes` | minutos |
| `12h`, `12hour`, `12hours` | horas |
| `7d`, `7day`, `7days` | dias |

O número precisa ser maior que zero. Unidades desconhecidas são rejeitadas.

### Exemplos

```text
/gerarchave 7d vip 5
/gerarchave 30d vip_gold 10 vip_gold
/gerarchave 12h evento 50
/gerarchave perm super 1
```

### Saída

```text
===== CHAVES GERADAS =====
Grupo: vip_gold
Duração: 30d
Quantidade: 10
Kit: vip_gold
A1B2C3D4E5F6
...
==========================
```

As chaves são salvas imediatamente no armazenamento configurado.

### Erros comuns

| Mensagem | Motivo |
|---|---|
| `Você não tem permissão para usar este comando.` | Falta `vipkeys.admin` e o executor não é OP. |
| `Duração inválida: '...'` | Formato ou unidade inválida. |
| `Quantidade deve ser um número entre 1 e N.` | Fora do intervalo permitido. |
| `Grupo não encontrado no LuckPerms: '...'` | O grupo não existe (quando o LuckPerms está disponível). |
| `Kit não encontrado: '...'` | O nome do kit informado não existe. |
| `Erro ao verificar o kit. Veja o console.` | Falha no armazenamento de kits. |

### Tab completion

- 1º argumento: `7d`, `30d`, `1h`, `12h`, `perm`.
- 2º argumento: grupos carregados do LuckPerms.
- 4º argumento: nomes de kits cadastrados.

---

## `/chave`

Resgata uma chave para o jogador que executou o comando.

```text
/chave <código>
```

### Regras

- Apenas jogadores podem executar; o console recebe uma mensagem de erro.
- O código não diferencia maiúsculas de minúsculas.
- A chave é de uso único.
- A chave precisa existir, não ter sido usada e não estar expirada.
- O LuckPerms precisa estar disponível e o grupo da chave precisa existir.
- Se a chave tiver kit, o kit precisa existir.
- Se o kit tiver `balance > 0`, o LlamaEconomy precisa estar disponível.

### Exemplo

```text
/chave A1B2C3D4E5F6
```

### Saída de sucesso

```text
Chave resgatada com sucesso!
Você foi adicionado ao grupo vip_gold (por 30d).
```

### Erros

| Situação | Mensagem |
|---|---|
| Chave inexistente | `Chave inválida.` |
| Chave já usada | `Chave já usada.` |
| Chave expirada | `Chave expirada.` |
| Sem LuckPerms | `LuckPerms não está disponível no servidor. Contate um administrador.` |
| Sem LlamaEconomy (kit com balance) | `LlamaEconomy não está disponível para creditar o balance deste kit.` |
| Grupo inexistente | `Grupo não encontrado no LuckPerms.` |
| Kit inexistente | `Kit associado à chave não encontrado.` |
| Falha interna | `Ocorreu um erro ao resgatar a chave.` |

> A chave **não** expira sozinha. A duração controla o tempo do **grupo** após o resgate.

---

## `/criarkitvip`

Captura todos os itens não vazios do inventário do jogador e cria/atualiza um kit.

```text
/criarkitvip <kit> <grupo> [balance]
```

### Argumentos

| Argumento | Tipo | Obrigatório | Regras |
|---|---|---|---|
| `kit` | texto | Sim | `^[a-z0-9_]+$`; precisa estar em minúsculas. |
| `grupo` | texto | Sim | Grupo LuckPerms associado ao kit (armazenado e exibido). |
| `balance` | decimal | Não | Valor `>= 0`. Padrão `0`. |

### Regras

- Apenas jogadores (o inventário é capturado do executor).
- O inventário não é limpo.
- Itens com NBT são preservados.
- O grupo do kit é armazenado para organização; o grupo concedido no resgate é o da chave.

### Exemplo

```text
/criarkitvip vip_gold vip_gold 250
```

### Saída

```text
Kit 'vip_gold' criado com sucesso!
Grupo: vip_gold
Balance: 250.0
Itens: 9
```

### Erros comuns

| Mensagem | Motivo |
|---|---|
| `Apenas jogadores podem criar kits...` | Executado pelo console. |
| `Nome do kit inválido...` | Nome fora de `^[a-z0-9_]+$`. |
| `Grupo não encontrado no LuckPerms: '...'` | Grupo inexistente (quando o LuckPerms está disponível). |
| `Balance deve ser maior ou igual a 0.` | Valor negativo. |
| `Seu inventário está vazio...` | Nenhum item válido no inventário. |
| `Erro ao salvar o kit. Veja o console.` | Falha no armazenamento. |

---

## `/editarkit`

Atualiza um kit existente, recapturando o inventário do jogador.

```text
/editarkit <kit> <grupo> [balance]
```

### Regras

- Apenas jogadores.
- O kit precisa existir.
- O inventário é recapturado (não é possível editar apenas grupo/balance sem recapturar itens).
- Se `balance` for omitido, o valor atual é mantido.
- `balance` negativo é rejeitado.
- O grupo é validado no LuckPerms quando ele está disponível.

### Exemplo

```text
/editarkit vip_gold vip_gold 500
```

### Saída

```text
Kit 'vip_gold' editado com sucesso!
Grupo: vip_gold
Balance: 500.0
Itens: 12
```

### Tab completion

- 1º argumento: kits cadastrados.
- 2º argumento: grupos do LuckPerms.

---

## `/listarkits`

Lista todos os kits salvos.

```text
/listarkits
```

### Saída

```text
===== KITS VIP =====
vip_gold | Grupo: vip_gold | Balance: 250.0 | Itens: 9
vip_super | Grupo: vip_super | Balance: 1000.0 | Itens: 15
====================
```

Sem kits:

```text
Nenhum kit cadastrado.
```

---

## `/apagarkit`

Apaga um kit do armazenamento.

```text
/apagarkit <kit>
```

### Regras

- O kit precisa existir.
- As chaves que referenciam o kit **não** são apagadas.
- Resgatar uma chave com kit apagado retorna `Kit associado à chave não encontrado.`

### Exemplo

```text
/apagarkit vip_gold
```

### Saída

```text
Kit 'vip_gold' apagado com sucesso!
```

### Tab completion

- 1º argumento: kits cadastrados.

---

## Permissões

| Permissão | Padrão | Comandos |
|---|---|---|
| `vipkeys.admin` | `op` | `/gerarchave`, `/criarkitvip`, `/editarkit`, `/listarkits`, `/apagarkit` |

O `/chave` não possui nó de permissão; qualquer jogador pode resgatar uma chave válida.
