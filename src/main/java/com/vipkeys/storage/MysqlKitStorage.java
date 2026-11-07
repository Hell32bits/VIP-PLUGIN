package com.vipkeys.storage;

import com.vipkeys.model.VipKit;
import com.vipkeys.util.ItemCodec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Armazenamento de kits em MySQL usando HikariCP.
 *
 * <p>Tabela {@code vip_kits}: {@code name}, {@code group_name}, {@code balance}, {@code items}.</p>
 */
public class MysqlKitStorage implements KitProvider {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    private HikariDataSource dataSource;
    private String table;

    public MysqlKitStorage(String host, int port, String database,
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
        hikari.setPoolName("VipKeysKitPool");
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(10_000L);

        this.dataSource = new HikariDataSource(hikari);
        this.table = tablePrefix + "vip_kits";
        createTable();
    }

    private void createTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                + "`name` VARCHAR(64) NOT NULL, "
                + "`group_name` VARCHAR(64) NOT NULL, "
                + "`balance` DOUBLE NOT NULL DEFAULT 0, "
                + "`items` TEXT NULL, "
                + "PRIMARY KEY (`name`))";
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    @Override
    public void save(VipKit kit) throws Exception {
        String sql = "REPLACE INTO `" + table + "` (name, group_name, balance, items) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kit.getName());
            ps.setString(2, kit.getLuckPermsGroup());
            ps.setDouble(3, kit.getBalance());
            ps.setString(4, ItemCodec.encodeToLine(kit.getItems()));
            ps.executeUpdate();
        }
    }

    @Override
    public VipKit get(String kitName) throws Exception {
        String sql = "SELECT name, group_name, balance, items FROM `" + table + "` WHERE name = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kitName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return read(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<VipKit> loadAll() throws Exception {
        List<VipKit> result = new ArrayList<>();
        String sql = "SELECT name, group_name, balance, items FROM `" + table + "`";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(read(rs));
            }
        }
        return result;
    }

    private VipKit read(ResultSet rs) throws SQLException {
        return new VipKit(
                rs.getString("name"),
                rs.getString("group_name"),
                rs.getDouble("balance"),
                ItemCodec.decodeFromLine(rs.getString("items"))
        );
    }

    @Override
    public void delete(String kitName) throws Exception {
        String sql = "DELETE FROM `" + table + "` WHERE name = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kitName);
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
