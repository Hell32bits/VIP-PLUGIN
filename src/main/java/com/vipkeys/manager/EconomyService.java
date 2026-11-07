package com.vipkeys.manager;

import com.vipkeys.VipKeysPlugin;
import net.lldv.llamaeconomy.LlamaEconomy;
import net.lldv.llamaeconomy.components.api.API;
import org.powernukkitx.Player;

/**
 * Fachada de integração com o LlamaEconomy.
 *
 * <p>Obtém a API via {@link LlamaEconomy#getAPI()} e expõe a operação de
 * creditar balance em um jogador. Se o LlamaEconomy não estiver instalado,
 * {@link #isAvailable()} retorna {@code false}.</p>
 */
public class EconomyService {

    private final VipKeysPlugin plugin;
    private final API api;

    public EconomyService(VipKeysPlugin plugin) {
        this.plugin = plugin;
        API a = null;
        try {
            a = LlamaEconomy.getAPI();
            plugin.getLogger().info("LlamaEconomy encontrado. Integração de economia ativada.");
        } catch (Throwable t) {
            plugin.getLogger().warning("LlamaEconomy não encontrado. Kits com balance não poderão creditar dinheiro.");
        }
        this.api = a;
    }

    /** Indica se o LlamaEconomy está disponível. */
    public boolean isAvailable() {
        return api != null;
    }

    /**
     * Credita {@code amount} no balance do jogador.
     *
     * <p>O build do LlamaEconomy para PowerNukkitX usa {@code org.powernukkitx.Player},
     * então o overload {@code addMoney(Player, double)} corresponde diretamente.</p>
     *
     * @return {@code true} em caso de sucesso
     */
    public boolean addMoney(Player player, double amount) {
        if (api == null || amount <= 0) {
            return false;
        }
        try {
            api.addMoney(player, amount);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().error("Erro ao creditar " + amount + " para " + player.getName() + ": " + t.getMessage(), t);
            return false;
        }
    }
}
