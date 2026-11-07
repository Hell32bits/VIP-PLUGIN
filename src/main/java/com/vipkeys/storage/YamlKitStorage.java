package com.vipkeys.storage;

import com.vipkeys.model.VipKit;
import com.vipkeys.util.ItemCodec;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.ConfigSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Armazenamento de kits em arquivo YAML ({@code kits.yml}).
 *
 * <pre>
 * kits:
 *   vip_gold:
 *     group: vip_gold
 *     balance: 250.0
 *     items:
 *       - "minecraft:diamond|0|64|"
 * </pre>
 */
public class YamlKitStorage implements KitProvider {

    private static final String SECTION = "kits";

    private final File file;
    private Config config;

    public YamlKitStorage(File file) {
        this.file = file;
    }

    @Override
    public void init() throws Exception {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        this.config = new Config(file, Config.YAML);
    }

    @Override
    public void save(VipKit kit) throws Exception {
        ConfigSection kits = config.getSection(SECTION);
        if (kits == null) {
            kits = new ConfigSection();
        }

        ConfigSection s = new ConfigSection();
        s.set("group", kit.getLuckPermsGroup());
        s.set("balance", kit.getBalance());
        s.set("items", ItemCodec.encodeAll(kit.getItems()));

        kits.set(kit.getName(), s);
        config.set(SECTION, kits);
        config.save();
    }

    @Override
    public VipKit get(String kitName) throws Exception {
        ConfigSection kits = config.getSection(SECTION);
        if (kits == null) {
            return null;
        }
        ConfigSection s = kits.getSection(kitName);
        return s == null ? null : read(kitName, s);
    }

    @Override
    public List<VipKit> loadAll() throws Exception {
        List<VipKit> result = new ArrayList<>();
        ConfigSection kits = config.getSection(SECTION);
        if (kits == null) {
            return result;
        }
        Set<String> names = kits.getKeys(false);
        for (String name : names) {
            ConfigSection s = kits.getSection(name);
            if (s != null) {
                result.add(read(name, s));
            }
        }
        return result;
    }

    private VipKit read(String name, ConfigSection s) {
        String group = s.getString("group", "");
        double balance = s.getDouble("balance", 0.0);
        List<String> items = s.getStringList("items");
        return new VipKit(name, group, balance, ItemCodec.decodeAll(items));
    }

    @Override
    public void delete(String kitName) throws Exception {
        ConfigSection kits = config.getSection(SECTION);
        if (kits != null) {
            kits.remove(kitName);
            config.set(SECTION, kits);
            config.save();
        }
    }

    @Override
    public void close() {
        // Nada a liberar no armazenamento YAML.
    }
}
