package com.vipkeys;

import com.vipkeys.command.ApagarKitCommand;
import com.vipkeys.command.ChaveCommand;
import com.vipkeys.command.CriarKitVipCommand;
import com.vipkeys.command.EditarKitCommand;
import com.vipkeys.command.GerarChaveCommand;
import com.vipkeys.command.ListarKitsCommand;
import com.vipkeys.manager.EconomyService;
import com.vipkeys.manager.KeyManager;
import com.vipkeys.manager.LuckPermsService;
import com.vipkeys.model.VipKit;
import com.vipkeys.storage.KitProvider;
import com.vipkeys.storage.KitStorageFactory;
import com.vipkeys.storage.StorageFactory;
import com.vipkeys.storage.StorageProvider;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal do plugin VipKeys.
 *
 * <p>Responsável por carregar a configuração, inicializar os armazenamentos
 * (chaves e kits), integrar com LuckPerms e LlamaEconomy e registrar os comandos.</p>
 */
public class VipKeysPlugin extends PluginBase {

    private static VipKeysPlugin instance;

    private Config config;
    private StorageProvider storage;
    private KitProvider kitProvider;
    private LuckPermsService luckPermsService;
    private EconomyService economyService;
    private KeyManager keyManager;

    private int keyLength = 12;
    private int maxQuantity = 100;

    public static VipKeysPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            // Garante que a pasta de dados exista e copia o config.yml padrão.
            this.getDataFolder().mkdirs();
            this.saveDefaultConfig();

            // Carrega a configuração a partir do arquivo.
            File configFile = new File(this.getDataFolder(), "config.yml");
            this.config = new Config(configFile, Config.YAML);

            // Opções gerais de chaves (com limites seguros).
            this.keyLength = clamp(config.getInt("keys.length", 12), 8, 32);
            this.maxQuantity = clamp(config.getInt("keys.maxQuantity", 100), 1, 10000);

            // Integrações (podem estar ausentes).
            this.luckPermsService = new LuckPermsService(this);
            this.economyService = new EconomyService(this);

            // Cria os armazenamentos conforme config.yml e os inicializa.
            this.storage = StorageFactory.create(this, this.config);
            this.storage.init();

            this.kitProvider = KitStorageFactory.create(this, this.config);
            this.kitProvider.init();

            // Carrega as chaves do armazenamento para a memória.
            this.keyManager = new KeyManager(this, this.storage, this.luckPermsService);
            this.keyManager.loadKeys();

            // Registra os comandos manualmente.
            this.getServer().getCommandMap().register(this.getName(), new GerarChaveCommand(this));
            this.getServer().getCommandMap().register(this.getName(), new ChaveCommand(this));
            this.getServer().getCommandMap().register(this.getName(), new CriarKitVipCommand(this));
            this.getServer().getCommandMap().register(this.getName(), new ListarKitsCommand(this));
            this.getServer().getCommandMap().register(this.getName(), new EditarKitCommand(this));
            this.getServer().getCommandMap().register(this.getName(), new ApagarKitCommand(this));

            this.getLogger().info(TextFormat.GREEN + "VipKeys habilitado com sucesso!");
            this.getLogger().info(TextFormat.GREEN + "Armazenamento: " + storage.getClass().getSimpleName()
                    + " | Kits: " + kitProvider.getClass().getSimpleName());
        } catch (Throwable t) {
            this.getLogger().error("Falha ao habilitar o VipKeys: " + t.getMessage(), t);
        }
    }

    @Override
    public void onDisable() {
        if (this.storage != null) {
            this.storage.close();
        }
        if (this.kitProvider != null) {
            this.kitProvider.close();
        }
        this.getLogger().info(TextFormat.RED + "VipKeys desabilitado.");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public Config getVipConfig() {
        return config;
    }

    public StorageProvider getStorage() {
        return storage;
    }

    public KitProvider getKitProvider() {
        return kitProvider;
    }

    /** Retorna os nomes de todos os kits cadastrados (para tab completion). */
    public List<String> getKitNames() {
        List<String> names = new ArrayList<>();
        try {
            for (VipKit kit : kitProvider.loadAll()) {
                names.add(kit.getName());
            }
        } catch (Throwable ignored) {
            // Ignora e retorna a lista (possivelmente vazia).
        }
        return names;
    }

    public LuckPermsService getLuckPermsService() {
        return luckPermsService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}
