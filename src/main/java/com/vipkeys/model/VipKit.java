package com.vipkeys.model;

import org.powernukkitx.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um Kit VIP entregue no resgate de uma chave.
 *
 * <p>Contém o nome do kit, o grupo LuckPerms associado, uma quantidade de
 * balance (economia) e a lista de itens ({@link Item}) que serão entregues.</p>
 */
public class VipKit {

    private final String name;
    private final String luckPermsGroup;
    private final double balance;
    private final List<Item> items;

    public VipKit(String name, String luckPermsGroup, double balance, List<Item> items) {
        this.name = name;
        this.luckPermsGroup = luckPermsGroup;
        this.balance = balance;
        this.items = items == null ? new ArrayList<>() : items;
    }

    public String getName() {
        return name;
    }

    public String getLuckPermsGroup() {
        return luckPermsGroup;
    }

    public double getBalance() {
        return balance;
    }

    public List<Item> getItems() {
        return items;
    }
}
