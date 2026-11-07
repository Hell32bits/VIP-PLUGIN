package com.vipkeys.command;

import com.vipkeys.VipKeysPlugin;
import com.vipkeys.model.VipKit;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.DoubleNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.inventory.Inventory;
import org.powernukkitx.item.Item;
import org.powernukkitx.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando administrativo {@code /editarkit <nomeKit> <grupo> [balance]}.
 *
 * <p>Re-captura os itens do inventário do jogador e atualiza o grupo e o balance
 * de um kit existente. Se o {@code balance} for omitido, mantém o valor atual.</p>
 */
public class EditarKitCommand extends Command {

    private final VipKeysPlugin plugin;

    public EditarKitCommand(VipKeysPlugin plugin) {
        super("editarkit", "Edita um kit VIP (re-captura os itens do inventário).", "/editarkit <nomeKit> <grupo> [balance]");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        List<String> groups = plugin.getLuckPermsService().getGroupNames();

        tree.getRoot().then(
                RouteNode.argument("nomeKit", new StringNode())
                        .suggest(plugin.getKitNames())
                        .then(
                                RouteNode.argument("grupo", new StringNode())
                                        .suggest(groups)
                                        .exec(this::executeCommand)
                                        .then(
                                                RouteNode.argument("balance", new DoubleNode()).exec(this::executeCommand)
                                        )
                        )
        );
    }

    private CommandResult executeCommand(CommandContext ctx) {
        CommandSender sender = ctx.getSender();

        if (!sender.isPlayer()) {
            return CommandResult.fail(TextFormat.RED + "Apenas jogadores podem editar kits (o inventário é re-capturado).");
        }
        if (!sender.hasPermission("vipkeys.admin") && !sender.isOp()) {
            return CommandResult.fail(TextFormat.RED + "Você não tem permissão para usar este comando.");
        }

        Player player = sender.asPlayer();

        String nomeKit = ctx.getArg("nomeKit");
        String grupo = ctx.getArg("grupo");
        Double balance = ctx.hasArg("balance") ? ctx.<Double>getArg("balance") : null;

        if (nomeKit == null || nomeKit.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Uso: /editarkit <nomeKit> <grupo> [balance]");
        }
        if (grupo == null || grupo.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Grupo inválido.");
        }
        if (plugin.getLuckPermsService().isAvailable() && !plugin.getLuckPermsService().groupExists(grupo)) {
            return CommandResult.fail(TextFormat.RED + "Grupo não encontrado no LuckPerms: '" + grupo + "'.");
        }

        VipKit existing;
        try {
            existing = plugin.getKitProvider().get(nomeKit);
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao buscar o kit '" + nomeKit + "': " + t.getMessage(), t);
            return CommandResult.fail(TextFormat.RED + "Erro ao buscar o kit. Veja o console.");
        }
        if (existing == null) {
            return CommandResult.fail(TextFormat.RED + "Kit não encontrado: '" + nomeKit + "'.");
        }

        // Se o balance for omitido, mantém o balance atual do kit.
        double newBalance = (balance == null) ? existing.getBalance() : balance;
        if (newBalance < 0) {
            return CommandResult.fail(TextFormat.RED + "Balance deve ser maior ou igual a 0.");
        }

        // Re-captura os itens do inventário (exceto vazios/air).
        List<Item> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (Item item : inv.getContents().values()) {
            if (item != null && !item.isNull()) {
                Item copy = item.clone();
                if (copy != null) {
                    items.add(copy);
                }
            }
        }
        if (items.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Seu inventário está vazio. Coloque os itens do kit no inventário antes de editar.");
        }

        VipKit updated = new VipKit(existing.getName(), grupo, newBalance, items);
        try {
            plugin.getKitProvider().save(updated);
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao salvar o kit '" + nomeKit + "': " + t.getMessage(), t);
            return CommandResult.fail(TextFormat.RED + "Erro ao salvar o kit. Veja o console.");
        }

        player.sendMessage(TextFormat.GREEN + "Kit '" + nomeKit + "' editado com sucesso!");
        player.sendMessage(TextFormat.GRAY + "Grupo: " + TextFormat.AQUA + grupo);
        player.sendMessage(TextFormat.GRAY + "Balance: " + TextFormat.GOLD + newBalance);
        player.sendMessage(TextFormat.GRAY + "Itens: " + TextFormat.AQUA + items.size());
        return CommandResult.success();
    }
}
