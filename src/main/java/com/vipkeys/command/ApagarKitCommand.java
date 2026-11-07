package com.vipkeys.command;

import com.vipkeys.VipKeysPlugin;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.utils.TextFormat;

/**
 * Comando administrativo {@code /apagarkit <nomeKit>}.
 *
 * <p>Remove um kit VIP do armazenamento.</p>
 */
public class ApagarKitCommand extends Command {

    private final VipKeysPlugin plugin;

    public ApagarKitCommand(VipKeysPlugin plugin) {
        super("apagarkit", "Apaga um kit VIP.", "/apagarkit <nomeKit>");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        tree.getRoot().then(
                RouteNode.argument("nomeKit", new StringNode())
                        .suggest(plugin.getKitNames())
                        .exec(this::executeCommand)
        );
    }

    private CommandResult executeCommand(CommandContext ctx) {
        CommandSender sender = ctx.getSender();

        if (!sender.hasPermission("vipkeys.admin") && !sender.isOp()) {
            return CommandResult.fail(TextFormat.RED + "Você não tem permissão para usar este comando.");
        }

        String nomeKit = ctx.getArg("nomeKit");
        if (nomeKit == null || nomeKit.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Uso: /apagarkit <nomeKit>");
        }

        try {
            if (plugin.getKitProvider().get(nomeKit) == null) {
                return CommandResult.fail(TextFormat.RED + "Kit não encontrado: '" + nomeKit + "'.");
            }
            plugin.getKitProvider().delete(nomeKit);
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao apagar o kit '" + nomeKit + "': " + t.getMessage(), t);
            return CommandResult.fail(TextFormat.RED + "Erro ao apagar o kit. Veja o console.");
        }

        sender.sendMessage(TextFormat.GREEN + "Kit '" + nomeKit + "' apagado com sucesso!");
        return CommandResult.success();
    }
}
