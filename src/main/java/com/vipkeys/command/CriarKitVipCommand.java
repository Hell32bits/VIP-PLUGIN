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
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Comando administrativo {@code /criarkitvip <nomeKit> <grupo> [balance]}.
 *
 * <p>Captura todos os itens do inventário do jogador (exceto air) e os salva
 * como um kit, associado a um grupo LuckPerms e a um balance opcional.</p>
 */
public class CriarKitVipCommand extends Command {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");

    private final VipKeysPlugin plugin;

    public CriarKitVipCommand(VipKeysPlugin plugin) {
        super("criarkitvip", "Cria um kit VIP a partir do seu inventário.", "/criarkitvip <nomeKit> <grupo> [balance]");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        List<String> groups = plugin.getLuckPermsService().getGroupNames();

        tree.getRoot().then(
                RouteNode.argument("nomeKit", new StringNode()).then(
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
            return CommandResult.fail(TextFormat.RED + "Apenas jogadores podem criar kits (o inventário é capturado do jogador).");
        }
        if (!sender.hasPermission("vipkeys.admin") && !sender.isOp()) {
            return CommandResult.fail(TextFormat.RED + "Você não tem permissão para usar este comando.");
        }

        Player player = sender.asPlayer();

        String nomeKit = ctx.getArg("nomeKit");
        String grupo = ctx.getArg("grupo");
        Double balance = ctx.hasArg("balance") ? ctx.<Double>getArg("balance") : 0.0;

        if (nomeKit == null || !NAME_PATTERN.matcher(nomeKit).matches()) {
            return CommandResult.fail(TextFormat.RED + "Nome do kit inválido. Use apenas letras minúsculas, números e '_'.");
        }
        if (grupo == null || grupo.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Grupo inválido.");
        }
        if (plugin.getLuckPermsService().isAvailable() && !plugin.getLuckPermsService().groupExists(grupo)) {
            return CommandResult.fail(TextFormat.RED + "Grupo não encontrado no LuckPerms: '" + grupo + "'.");
        }
        if (balance == null || balance < 0) {
            return CommandResult.fail(TextFormat.RED + "Balance deve ser maior ou igual a 0.");
        }

        // Captura os itens do inventário (exceto vazios/air).
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
            return CommandResult.fail(TextFormat.RED + "Seu inventário está vazio. Coloque os itens do kit no inventário antes de usar o comando.");
        }

        VipKit kit = new VipKit(nomeKit.toLowerCase(Locale.ROOT), grupo, balance, items);
        try {
            plugin.getKitProvider().save(kit);
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao salvar o kit '" + nomeKit + "': " + t.getMessage(), t);
            return CommandResult.fail(TextFormat.RED + "Erro ao salvar o kit. Veja o console.");
        }

        player.sendMessage(TextFormat.GREEN + "Kit '" + kit.getName() + "' criado com sucesso!");
        player.sendMessage(TextFormat.GRAY + "Grupo: " + TextFormat.AQUA + grupo);
        player.sendMessage(TextFormat.GRAY + "Balance: " + TextFormat.GOLD + balance);
        player.sendMessage(TextFormat.GRAY + "Itens: " + TextFormat.AQUA + items.size());
        return CommandResult.success();
    }
}
