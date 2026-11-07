package com.vipkeys.command;

import com.vipkeys.VipKeysPlugin;
import com.vipkeys.model.VipKit;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.utils.TextFormat;

import java.util.List;

/**
 * Comando administrativo {@code /listarkits}.
 *
 * <p>Lista todos os kits salvos com nome, grupo, balance e quantidade de itens.</p>
 */
public class ListarKitsCommand extends Command {

    private final VipKeysPlugin plugin;

    public ListarKitsCommand(VipKeysPlugin plugin) {
        super("listarkits", "Lista todos os kits VIP.", "/listarkits");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        tree.getRoot().exec(this::executeCommand);
    }

    private CommandResult executeCommand(CommandContext ctx) {
        CommandSender sender = ctx.getSender();

        if (!sender.hasPermission("vipkeys.admin") && !sender.isOp()) {
            return CommandResult.fail(TextFormat.RED + "Você não tem permissão para usar este comando.");
        }

        List<VipKit> kits;
        try {
            kits = plugin.getKitProvider().loadAll();
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao listar kits: " + t.getMessage(), t);
            return CommandResult.fail(TextFormat.RED + "Erro ao listar os kits. Veja o console.");
        }

        if (kits.isEmpty()) {
            sender.sendMessage(TextFormat.YELLOW + "Nenhum kit cadastrado.");
            return CommandResult.success();
        }

        sender.sendMessage(TextFormat.GREEN + "===== KITS VIP =====");
        for (VipKit kit : kits) {
            sender.sendMessage(TextFormat.AQUA + kit.getName()
                    + TextFormat.GRAY + " | Grupo: " + TextFormat.GREEN + kit.getLuckPermsGroup()
                    + TextFormat.GRAY + " | Balance: " + TextFormat.GOLD + kit.getBalance()
                    + TextFormat.GRAY + " | Itens: " + TextFormat.AQUA + kit.getItems().size());
        }
        sender.sendMessage(TextFormat.GREEN + "====================");
        return CommandResult.success();
    }
}
