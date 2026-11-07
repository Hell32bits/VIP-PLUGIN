package com.vipkeys.storage;

import com.vipkeys.model.VipKey;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Armazenamento em MySQL usando pool de conexões HikariCP.
 *
 * <p>Tabela criada automaticamente:</p>
 * <pre>
 * vip_keys (
 *   code        VARCHAR(12) PRIMARY KEY,
 *   group_name  VARCHAR(64),
 *   duration    VARCHAR(32),
 *   duration_ms BIGINT,
 *   created_at  BIGINT,
 *   expires_at  BIGINT,
 *   used        TINYINT(1),
 *   used_by     VARCHAR(64),
 *   used_at     BIGINT
 * )
 * </pre>
 */
public class MysqlStorage implements StorageProvider {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    private HikariDataSource dataSource;
    private String table;

    public MysqlStorage(String host, int port, String database,
                        String username, String password, String tablePrefix) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix == null ? "" : tablePrefix;
    }

    @Override
    public void init() throws Exception {
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&characterEncoding=UTF-8";

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setPoolName("VipKeysPool");
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10_000L);
        hikari.setMaxLifetime(1_800_000L);

        this.dataSource = new HikariDataSource(hikari);
        this.table = tablePrefix + "vip_keys";
        createTable();
    }

    private void createTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                + "`code` VARCHAR(12) NOT NULL, "
                + "`group_name` VARCHAR(64) NOT NULL, "
                + "`duration` VARCHAR(32) NOT NULL, "
                + "`duration_ms` BIGINT NOT NULL, "
                + "`created_at` BIGINT NOT NULL, "
                + "`expires_at` BIGINT NOT NULL DEFAULT 0, "
                + "`used` TINYINT(1) NOT NULL DEFAULT 0, "
                + "`used_by` VARCHAR(64) NULL, "
                + "`used_at` BIGINT NOT NULL DEFAULT 0, "
                + "`kit_name` VARCHAR(64) NULL, "
                + "PRIMARY KEY (`code`)"
                + ")";
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    @Override
    public List<VipKey> loadAll() throws Exception {
        List<VipKey> result = new ArrayList<>();
        String sql = "SELECT code, group_name, duration, duration_ms, created_at, expires_at, used, used_by, used_at, kit_name FROM `" + table + "`";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String usedBy = rs.getString("used_by");
                if (usedBy != null && usedBy.isEmpty()) {
                    usedBy = null;
                }
                result.add(new VipKey(
                        rs.getString("code"),
                        rs.getString("group_name"),
                        rs.getString("duration"),
                        rs.getLong("duration_ms"),
                        rs.getLong("created_at"),
                        rs.getLong("expires_at"),
                        rs.getBoolean("used"),
                        usedBy,
                        rs.getLong("used_at"),
                        rs.getString("kit_name")
                ));
            }
        }
        return result;
    }

    @Override
    public void save(VipKey key) throws Exception {
        String sql = "REPLACE INTO `" + table + "` "
                + "(code, group_name, duration, duration_ms, created_at, expires_at, used, used_by, used_at, kit_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key.getCode());
            ps.setString(2, key.getGroup());
            ps.setString(3, key.getDuration());
            ps.setLong(4, key.getDurationMs());
            ps.setLong(5, key.getCreatedAt());
            ps.setLong(6, key.getExpiresAt());
            ps.setBoolean(7, key.isUsed());
            ps.setString(8, key.getUsedBy());
            ps.setLong(9, key.getUsedAt());
            ps.setString(10, key.getKitName());
            ps.executeUpdate();
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
