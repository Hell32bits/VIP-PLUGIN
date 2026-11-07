package com.vipkeys.storage;

import com.vipkeys.VipKeysPlugin;
import org.powernukkitx.utils.Config;

import java.io.File;

/**
 * Fábrica que escolhe a implementação de armazenamento conforme o {@code config.yml}.
 */
public final class StorageFactory {

    private StorageFactory() {
    }

    /**
     * Cria o {@link StorageProvider} apropriado com base em {@code storage.type}.
     *
     * @param plugin a instância do plugin
     * @param config a configuração já carregada
     * @return uma implementação de {@link StorageProvider}
     */
    public static StorageProvider create(VipKeysPlugin plugin, Config config) {
        String type = config.getString("storage.type", "yml");
        if ("mysql".equalsIgnoreCase(type)) {
            String host = config.getString("storage.mysql.host", "localhost");
            int port = config.getInt("storage.mysql.port", 3306);
            String database = config.getString("storage.mysql.database", "vipkeys");
            String username = config.getString("storage.mysql.username", "root");
            String password = config.getString("storage.mysql.password", "");
            String tablePrefix = config.getString("storage.mysql.tablePrefix", "");
            return new MysqlStorage(host, port, database, username, password, tablePrefix);
        }

        // Padrão: YML
        String filePath = config.getString("storage.yml.file", "chaves.yml");
        File file = new File(plugin.getDataFolder(), filePath);
        return new YamlStorage(file);
    }
}
