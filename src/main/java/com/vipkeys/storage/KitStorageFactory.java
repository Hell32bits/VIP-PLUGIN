package com.vipkeys.storage;

import com.vipkeys.VipKeysPlugin;
import org.powernukkitx.utils.Config;

import java.io.File;

/**
 * Fábrica que escolhe a implementação de armazenamento de kits conforme o {@code config.yml}.
 */
public final class KitStorageFactory {

    private KitStorageFactory() {
    }

    public static KitProvider create(VipKeysPlugin plugin, Config config) {
        String type = config.getString("storage.type", "yml");
        if ("mysql".equalsIgnoreCase(type)) {
            String host = config.getString("storage.mysql.host", "localhost");
            int port = config.getInt("storage.mysql.port", 3306);
            String database = config.getString("storage.mysql.database", "vipkeys");
            String username = config.getString("storage.mysql.username", "root");
            String password = config.getString("storage.mysql.password", "");
            String tablePrefix = config.getString("storage.mysql.tablePrefix", "");
            return new MysqlKitStorage(host, port, database, username, password, tablePrefix);
        }

        // Padrão: YML
        String filePath = config.getString("storage.yml.kitFile", "kits.yml");
        File file = new File(plugin.getDataFolder(), filePath);
        return new YamlKitStorage(file);
    }
}
