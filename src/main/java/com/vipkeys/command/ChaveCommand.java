package com.vipkeys.command;

import com.vipkeys.VipKeysPlugin;
import com.vipkeys.manager.RedeemResult;
import com.vipkeys.model.VipKey;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.utils.TextFormat;

/**
 * Comando de jogador {@code /chave <codigo>}.
 *
 * <p>Valida a chave e, em caso de sucesso, adiciona o jogador ao grupo
 * correspondente no LuckPerms e agenda a remoção (se o VIP tiver duração).</p>
 */
public class ChaveCommand extends Command {

    private final VipKeysPlugin plugin;

    public ChaveCommand(VipKeysPlugin plugin) {
        super("chave", "Resgata uma chave de VIP.", "/chave <codigo>");
        this.plugin = plugin;
        this.enableCommandTree();
    }

    @Override
    protected void buildCommandTree(RouteTree tree) {
        tree.getRoot().then(
                RouteNode.argument("codigo", new StringNode()).exec(this::executeCommand)
        );
    }

    private CommandResult executeCommand(CommandContext ctx) {
        CommandSender sender = ctx.getSender();

        if (!sender.isPlayer()) {
            return CommandResult.fail(TextFormat.RED + "Apenas jogadores podem resgatar chaves.");
        }

        Player player = sender.asPlayer();
        String codigo = ctx.getArg("codigo");

        if (codigo == null || codigo.isEmpty()) {
            return CommandResult.fail(TextFormat.RED + "Uso: /chave <codigo>");
        }

        RedeemResult result = plugin.getKeyManager().redeem(player, codigo);

        if (result == RedeemResult.SUCCESS) {
            VipKey key = plugin.getKeyManager().getKey(codigo);
            player.sendMessage(TextFormat.GREEN + "Chave resgatada com sucesso!");
            if (key != null) {
                String duracao = key.isPermanent() ? "permanente" : ("por " + key.getDuration());
                player.sendMessage(TextFormat.GRAY + "Você foi adicionado ao grupo "
                        + TextFormat.AQUA + key.getGroup() + TextFormat.GRAY + " (" + duracao + ").");
            }
            return CommandResult.success();
        }

        return CommandResult.fail(TextFormat.RED + result.getMessage());
    }
}
