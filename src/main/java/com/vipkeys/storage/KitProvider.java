package com.vipkeys.storage;

import com.vipkeys.model.VipKit;

import java.util.List;

/**
 * Contrato de armazenamento dos kits VIP.
 *
 * <p>Implementações: {@link YamlKitStorage} (padrão) e {@link MysqlKitStorage}.</p>
 */
public interface KitProvider {

    /** Abre/conecta o armazenamento. Lança exceção em caso de falha. */
    void init() throws Exception;

    /** Insere ou atualiza (upsert) um kit. */
    void save(VipKit kit) throws Exception;

    /** Busca um kit pelo nome (ou {@code null} se não existir). */
    VipKit get(String kitName) throws Exception;

    /** Carrega todos os kits. */
    List<VipKit> loadAll() throws Exception;

    /** Remove um kit pelo nome. */
    void delete(String kitName) throws Exception;

    /** Libera recursos (conexões etc.). */
    void close();
}
