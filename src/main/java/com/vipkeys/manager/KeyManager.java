package com.vipkeys.manager;

import com.vipkeys.VipKeysPlugin;
import com.vipkeys.model.VipKey;
import com.vipkeys.model.VipKit;
import com.vipkeys.storage.StorageProvider;
import com.vipkeys.util.KeyGenerator;
import org.powernukkitx.Player;
import org.powernukkitx.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o ciclo de vida das chaves: geração, consulta e resgate.
 */
public class KeyManager {

    private final VipKeysPlugin plugin;
    private final StorageProvider storage;
    private final LuckPermsService luckPerms;
    private final Map<String, VipKey> keys = new ConcurrentHashMap<>();

    public KeyManager(VipKeysPlugin plugin, StorageProvider storage, LuckPermsService luckPerms) {
        this.plugin = plugin;
        this.storage = storage;
        this.luckPerms = luckPerms;
    }

    /** Carrega todas as chaves do armazenamento para a memória. */
    public void loadKeys() {
        keys.clear();
        try {
            List<VipKey> loaded = storage.loadAll();
            for (VipKey key : loaded) {
                keys.put(key.getCode(), key);
            }
            plugin.getLogger().info(loaded.size() + " chave(s) carregada(s).");
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao carregar as chaves: " + t.getMessage(), t);
        }
    }

    /** Persiste todas as chaves em memória (útil antes de desligar). */
    public void saveAll() {
        for (VipKey key : keys.values()) {
            try {
                storage.save(key);
            } catch (Throwable t) {
                plugin.getLogger().error("Falha ao salvar a chave " + key.getCode() + ": " + t.getMessage(), t);
            }
        }
    }

    /** Busca uma chave pelo código (insensível a maiúsculas/minúsculas). */
    public VipKey getKey(String code) {
        if (code == null) {
            return null;
        }
        return keys.get(code.toUpperCase(Locale.ROOT));
    }

    /**
     * Gera {@code quantity} chaves para o grupo/duração informados.
     *
     * @param durationStr duração em texto (ex.: "7d")
     * @param durationMs  duração já convertida em ms (-1 = permanente)
     * @param kitName     nome do kit associado (ou {@code null})
     * @return as chaves geradas
     */
    public List<VipKey> generateKeys(String durationStr, long durationMs, String group, int quantity, String kitName) {
        List<VipKey> generated = new ArrayList<>(quantity);
        long now = System.currentTimeMillis();
        for (int i = 0; i < quantity; i++) {
            String code;
            do {
                code = KeyGenerator.generate(plugin.getKeyLength());
            } while (keys.containsKey(code));

            VipKey key = new VipKey(code, group, durationStr, durationMs, now, 0L, false, null, 0L, kitName);
            keys.put(code, key);
            generated.add(key);

            try {
                storage.save(key);
            } catch (Throwable t) {
                plugin.getLogger().error("Falha ao salvar a chave " + code + ": " + t.getMessage(), t);
            }
        }
        return generated;
    }

    /**
     * Tenta resgatar uma chave para o jogador informado.
     *
     * <p>Valida a chave, o grupo LuckPerms e o kit (se houver). Em caso de sucesso,
     * adiciona o grupo (com expiry do LuckPerms, se não for permanente), entrega o kit
     * (itens + balance via LlamaEconomy) e marca a chave como usada.</p>
     */
    public RedeemResult redeem(Player player, String code) {
        VipKey key = getKey(code);
        if (key == null) {
            return RedeemResult.INVALID;
        }
        if (key.isUsed()) {
            return RedeemResult.ALREADY_USED;
        }
        if (key.isExpired()) {
            return RedeemResult.EXPIRED;
        }
        if (!luckPerms.isAvailable()) {
            return RedeemResult.NO_LUCKPERMS;
        }
        if (!luckPerms.groupExists(key.getGroup())) {
            return RedeemResult.GROUP_NOT_FOUND;
        }

        // Resolve o kit associado (se houver) e valida a economia antes de qualquer efeito colateral.
        VipKit kit = null;
        if (key.getKitName() != null && !key.getKitName().isEmpty()) {
            try {
                kit = plugin.getKitProvider().get(key.getKitName());
            } catch (Throwable t) {
                plugin.getLogger().error("Falha ao buscar o kit '" + key.getKitName() + "': " + t.getMessage(), t);
                kit = null;
            }
            if (kit == null) {
                return RedeemResult.KIT_NOT_FOUND;
            }
            if (kit.getBalance() > 0 && !plugin.getEconomyService().isAvailable()) {
                return RedeemResult.NO_ECONOMY;
            }
        }

        UUID uuid = player.getUniqueId();
        String group = key.getGroup();

        if (!luckPerms.addGroup(uuid, group, key.getDurationMs())) {
            return RedeemResult.ERROR;
        }

        if (kit != null) {
            deliverKit(player, kit);
        }

        key.setUsed(true);
        key.setUsedBy(uuid.toString());
        key.setUsedAt(System.currentTimeMillis());
        try {
            storage.save(key);
        } catch (Throwable t) {
            plugin.getLogger().error("Falha ao persistir o uso da chave " + key.getCode() + ": " + t.getMessage(), t);
        }

        return RedeemResult.SUCCESS;
    }

    /** Entrega os itens do kit (dropa no chão o que não couber) e credita o balance. */
    private void deliverKit(Player player, VipKit kit) {
        List<Item> items = kit.getItems();
        Item[] arr = new Item[items.size()];
        for (int i = 0; i < arr.length; i++) {
            Item copy = items.get(i).clone();
            arr[i] = copy != null ? copy : items.get(i);
        }
        Item[] remaining = player.getInventory().addItem(arr);
        for (Item leftover : remaining) {
            player.dropItem(leftover);
        }
        if (kit.getBalance() > 0) {
            plugin.getEconomyService().addMoney(player, kit.getBalance());
        }
    }
}
