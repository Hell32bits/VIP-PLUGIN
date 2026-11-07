package com.vipkeys.command;

import com.vipkeys.VipKeysPlugin;
import com.vipkeys.model.VipKey;
import com.vipkeys.util.DurationParser;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.IntNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.utils.TextFormat;

import java.util.List;

/**
 * Comando administrativo {@code /gerarchave <duração> <grupo> <quantidade> [kitName]}.
 *
 * <p>Gera chaves de VIP de 12 caracteres e as persiste no armazenamento.
 * Se {@code kitName} for informado, as chaves ficam associadas a um kit VIP.</p>
 */
public class GerarChaveCommand extends Command {

    private final VipKeysPlugin plugin;

    public GerarChaveCommand(VipKeysPlugin plugin) {
        super("gerarchave", "Gera chaves de VIP (Admin).", "/gerarchave <duração> <grupo> <quantidade> [kitName]");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        List<String> durations = List.of("7d", "30d", "1h", "12h", "perm");
        List<String> groups = plugin.getLuckPermsService().getGroupNames();
        List<String> kitNames = plugin.getKitNames();

        tree.getRoot().then(
                RouteNode.argument("duracao", new StringNode())
                        .suggest(durations)
                        .then(
                                RouteNode.argument("grupo", new StringNode())
                                        .suggest(groups)
                                        .then(
                                                RouteNode.argument("quantidade", new IntNode())
                                                        .exec(this::executeCommand)
                                                        .then(
                                                                RouteNode.argument("kitName", new StringNode())
                                                                        .suggest(kitNames)
                                                                        .exec(this::executeCommand)
                                                        )
                                        )
                        )
        );
    }

    private CommandResult executeCommand(CommandContext ctx) {
        CommandSender sender = ctx.getSender();

        if (!sender.hasPermission("vipkeys.admin") && !sender.isOp()) {
            return CommandResult.fail(TextFormat.RED + "Você não tem permissão para usar este comando.");
        }

        String duracao = ctx.getArg("duracao");
        String grupo = ctx.getArg("grupo");
        Integer quantidade = ctx.getArg("quantidade");
        String kitName = ctx.hasArg("kitName") ? ctx.<String>getArg("kitName") : null;

        if (duracao == null || grupo == null || quantidade == null) {
            return CommandResult.fail(TextFormat.RED + "Uso: /gerarchave <duração> <grupo> <quantidade> [kitName]");
        }

        long durationMs;
        try {
            durationMs = DurationParser.parse(duracao);
        } catch (IllegalArgumentException e) {
            return CommandResult.fail(TextFormat.RED + "Duração inválida: '" + duracao
                    + "'. Use ex.: 7d, 30d, 1h, 12h ou perm.");
        }

        int max = plugin.getMaxQuantity();
        if (quantidade < 1 || quantidade > max) {
            return CommandResult.fail(TextFormat.RED + "Quantidade deve ser um número entre 1 e " + max + ".");
        }

        if (plugin.getLuckPermsService().isAvailable() && !plugin.getLuckPermsService().groupExists(grupo)) {
            return CommandResult.fail(TextFormat.RED + "Grupo não encontrado no LuckPerms: '" + grupo + "'.");
        }

        // Valida o kit, se informado.
        if (kitName != null && !kitName.isEmpty()) {
            try {
                if (plugin.getKitProvider().get(kitName) == null) {
                    return CommandResult.fail(TextFormat.RED + "Kit não encontrado: '" + kitName + "'.");
                }
            } catch (Throwable t) {
                plugin.getLogger().error("Falha ao buscar o kit '" + kitName + "': " + t.getMessage(), t);
                return CommandResult.fail(TextFormat.RED + "Erro ao verificar o kit. Veja o console.");
            }
        }

        List<VipKey> keys = plugin.getKeyManager().generateKeys(duracao, durationMs, grupo, quantidade, kitName);

        sender.sendMessage(TextFormat.GREEN + "===== CHAVES GERADAS =====");
        sender.sendMessage(TextFormat.GRAY + "Grupo: " + TextFormat.AQUA + grupo);
        sender.sendMessage(TextFormat.GRAY + "Duração: " + TextFormat.AQUA + duracao
                + (durationMs < 0 ? " (permanente)" : ""));
        sender.sendMessage(TextFormat.GRAY + "Quantidade: " + TextFormat.AQUA + quantidade);
        if (kitName != null && !kitName.isEmpty()) {
            sender.sendMessage(TextFormat.GRAY + "Kit: " + TextFormat.AQUA + kitName);
        }
        for (VipKey key : keys) {
            sender.sendMessage(TextFormat.GREEN + key.getCode());
        }
        sender.sendMessage(TextFormat.GREEN + "==========================");

        return CommandResult.success();
    }
}
