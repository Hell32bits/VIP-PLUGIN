package com.vipkeys.storage;

import com.vipkeys.model.VipKey;

import java.util.List;

/**
 * Contrato de armazenamento das chaves de VIP.
 *
 * <p>Implementações: {@link YamlStorage} (padrão) e {@link MysqlStorage}.</p>
 */
public interface StorageProvider {

    /** Abre/conecta o armazenamento. Lança exceção em caso de falha. */
    void init() throws Exception;

    /** Carrega todas as chaves do armazenamento. */
    List<VipKey> loadAll() throws Exception;

    /** Insere ou atualiza (upsert) uma chave no armazenamento. */
    void save(VipKey key) throws Exception;

    /** Libera recursos (conexões, arquivos etc.). */
    void close();
}
