package com.vipkeys.manager;

import com.vipkeys.VipKeysPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Fachada de integração com o LuckPerms.
 *
 * <p>Obtém a instância do LuckPerms via {@link LuckPermsProvider#get()} e fornece
 * operações de leitura de grupos e de adição/remoção de nós de herança
 * ({@link InheritanceNode}) nos usuários.</p>
 */
public class LuckPermsService {

    private final VipKeysPlugin plugin;
    private final LuckPerms luckPerms;

    public LuckPermsService(VipKeysPlugin plugin) {
        this.plugin = plugin;
        LuckPerms lp = null;
        try {
            lp = LuckPermsProvider.get();
            plugin.getLogger().info("LuckPerms encontrado. Integração de grupos ativada.");
        } catch (Throwable t) {
            plugin.getLogger().warning("LuckPerms não encontrado. Não será possível conceder grupos ao resgatar chaves.");
        }
        this.luckPerms = lp;
    }

    /** Indica se o LuckPerms está disponível. */
    public boolean isAvailable() {
        return luckPerms != null;
    }

    /** Verifica se um grupo existe no LuckPerms. */
    public boolean groupExists(String groupName) {
        if (luckPerms == null || groupName == null) {
            return false;
        }
        try {
            return luckPerms.getGroupManager().getGroup(groupName) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Retorna os nomes de todos os grupos registrados no LuckPerms. */
    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        if (luckPerms == null) {
            return names;
        }
        try {
            GroupManager groupManager = luckPerms.getGroupManager();
            // loadAllGroups() retorna CompletableFuture<Void>: força o carregamento de
            // todos os grupos do storage para a memória (bloqueia até concluir).
            groupManager.loadAllGroups().join();
            for (Group group : groupManager.getLoadedGroups()) {
                names.add(group.getName());
            }
        } catch (Throwable ignored) {
            // Ignora e retorna a lista (possivelmente vazia).
        }
        return names;
    }

    /**
     * Adiciona um nó de herança (grupo) a um jogador pelo UUID.
     *
     * <p>Se {@code durationMs >= 0}, o nó recebe um {@code expiry} do LuckPerms,
     * garantindo remoção persistente mesmo após reinício do servidor.
     * Use {@code durationMs < 0} para VIP permanente (sem expiração).</p>
     *
     * @return {@code true} em caso de sucesso
     */
    public boolean addGroup(UUID uuid, String group, long durationMs) {
        if (luckPerms == null) {
            return false;
        }
        try {
            UserManager userManager = luckPerms.getUserManager();
            CompletableFuture<Void> future = userManager.modifyUser(uuid, user -> {
                InheritanceNode.Builder builder = InheritanceNode.builder(group).value(true);
                if (durationMs >= 0) {
                    builder.expiry(Duration.ofMillis(durationMs));
                }
                user.data().add(builder.build());
            });
            future.join();
            return true;
        } catch (Throwable t) {
            plugin.getLogger().error("Erro ao adicionar grupo '" + group + "' ao jogador " + uuid + ": " + t.getMessage(), t);
            return false;
        }
    }

    /**
     * Remove um nó de herança (grupo) de um jogador pelo UUID.
     *
     * @return {@code true} em caso de sucesso
     */
    public boolean removeGroup(UUID uuid, String group) {
        if (luckPerms == null) {
            return false;
        }
        try {
            UserManager userManager = luckPerms.getUserManager();
            CompletableFuture<Void> future = userManager.modifyUser(uuid, user ->
                    user.data().remove(InheritanceNode.builder(group).value(true).build()));
            future.join();
            return true;
        } catch (Throwable t) {
            plugin.getLogger().error("Erro ao remover grupo '" + group + "' do jogador " + uuid + ": " + t.getMessage(), t);
            return false;
        }
    }
}
