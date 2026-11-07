package com.vipkeys.storage;

import com.vipkeys.model.VipKey;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.ConfigSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Armazenamento em arquivo YAML (padrão).
 *
 * <p>Estrutura do arquivo {@code chaves.yml}:</p>
 * <pre>
 * keys:
 *   A1B2C3D4E5F6:
 *     group: vip
 *     duration: 7d
 *     durationMs: 604800000
 *     createdAt: 1700000000000
 *     expiresAt: 0
 *     used: false
 *     usedBy: ""
 *     usedAt: 0
 * </pre>
 */
public class YamlStorage implements StorageProvider {

    private static final String SECTION = "keys";

    private final File file;
    private Config config;

    public YamlStorage(File file) {
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
    public List<VipKey> loadAll() throws Exception {
        List<VipKey> result = new ArrayList<>();
        ConfigSection keysSection = config.getSection(SECTION);
        if (keysSection == null) {
            return result;
        }
        Set<String> codes = keysSection.getKeys(false);
        for (String code : codes) {
            ConfigSection s = keysSection.getSection(code);
            if (s == null) {
                continue;
            }
            String group = s.getString("group", "");
            String duration = s.getString("duration", "perm");
            long durationMs = s.getLong("durationMs", -1L);
            long createdAt = s.getLong("createdAt", 0L);
            long expiresAt = s.getLong("expiresAt", 0L);
            boolean used = s.getBoolean("used", false);
            String usedBy = s.getString("usedBy", "");
            if (usedBy != null && usedBy.isEmpty()) {
                usedBy = null;
            }
            long usedAt = s.getLong("usedAt", 0L);
            String kitName = s.getString("kitName", "");
            if (kitName != null && kitName.isEmpty()) {
                kitName = null;
            }
            result.add(new VipKey(code, group, duration, durationMs, createdAt, expiresAt, used, usedBy, usedAt, kitName));
        }
        return result;
    }

    @Override
    public void save(VipKey key) throws Exception {
        ConfigSection keysSection = config.getSection(SECTION);
        if (keysSection == null) {
            keysSection = new ConfigSection();
        }

        ConfigSection s = new ConfigSection();
        s.set("group", key.getGroup());
        s.set("duration", key.getDuration());
        s.set("durationMs", key.getDurationMs());
        s.set("createdAt", key.getCreatedAt());
        s.set("expiresAt", key.getExpiresAt());
        s.set("used", key.isUsed());
        s.set("usedBy", key.getUsedBy() == null ? "" : key.getUsedBy());
        s.set("usedAt", key.getUsedAt());
        s.set("kitName", key.getKitName() == null ? "" : key.getKitName());

        keysSection.set(key.getCode(), s);
        // Re-atribui a seção ao config para garantir a persistência.
        config.set(SECTION, keysSection);
        config.save();
    }

    @Override
    public void close() {
        // Nada a liberar no armazenamento YAML.
    }
}
