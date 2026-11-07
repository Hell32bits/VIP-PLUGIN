package com.vipkeys.util;

import org.powernukkitx.item.Item;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Serializa e desserializa {@link Item} para armazenamento em texto.
 *
 * <p>Formato de cada item: {@code id|meta|count|base64nbt}, onde a parte NBT
 * (encantamentos, nome customizado, lore etc.) é opcional e codificada em Base64.</p>
 */
public final class ItemCodec {

    private static final String SEP = "|";

    private ItemCodec() {
    }

    /** Converte um item para a representação textual. */
    public static String encode(Item item) {
        String nbt = "";
        if (item.hasNbt()) {
            nbt = Base64.getEncoder().encodeToString(item.getNbtBytes());
        }
        return item.getId() + SEP + item.getDamage() + SEP + item.getCount() + SEP + nbt;
    }

    /** Reconstrói um item a partir da representação textual. */
    public static Item decode(String encoded) {
        String[] parts = encoded.split("\\" + SEP, 4);
        String id = parts[0];
        int meta = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int count = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
        Item item = Item.get(id, meta, count);
        if (parts.length > 3 && !parts[3].isEmpty()) {
            item.setNbtBytes(Base64.getDecoder().decode(parts[3]));
        }
        return item;
    }

    /** Converte uma lista de itens para uma lista de strings (para YAML). */
    public static List<String> encodeAll(List<Item> items) {
        List<String> out = new ArrayList<>();
        if (items != null) {
            for (Item item : items) {
                out.add(encode(item));
            }
        }
        return out;
    }

    /** Reconstrói uma lista de itens a partir de uma lista de strings (do YAML). */
    public static List<Item> decodeAll(List<String> encoded) {
        List<Item> out = new ArrayList<>();
        if (encoded != null) {
            for (String s : encoded) {
                if (s != null && !s.isEmpty()) {
                    out.add(decode(s));
                }
            }
        }
        return out;
    }

    /** Converte a lista de itens em uma única string (para coluna TEXT do MySQL). */
    public static String encodeToLine(List<Item> items) {
        return String.join("\n", encodeAll(items));
    }

    /** Reconstrói a lista de itens a partir da string única (do MySQL). */
    public static List<Item> decodeFromLine(String line) {
        if (line == null || line.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> encoded = new ArrayList<>();
        for (String s : line.split("\n")) {
            encoded.add(s);
        }
        return decodeAll(encoded);
    }
}
