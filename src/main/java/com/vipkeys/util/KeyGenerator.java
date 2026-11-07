package com.vipkeys.util;

import java.security.SecureRandom;
import java.util.Set;

/**
 * Gera códigos de chave aleatórios e únicos.
 *
 * <p>Cada chave contém apenas letras maiúsculas (A-Z) e números (0-9),
 * sem caracteres especiais. O comprimento padrão é 12 (ex.: {@code A1B2C3D4E5F6}).</p>
 */
public final class KeyGenerator {

    public static final int DEFAULT_LENGTH = 12;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyGenerator() {
    }

    /** Gera uma chave com o comprimento padrão (12). */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Gera uma chave com o comprimento informado, garantindo unicidade
     * em relação ao conjunto {@code existing}.
     */
    public static String generate(int length, Set<String> existing) {
        if (length <= 0) {
            length = DEFAULT_LENGTH;
        }
        StringBuilder sb = new StringBuilder(length);
        String key;
        do {
            sb.setLength(0);
            for (int i = 0; i < length; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            key = sb.toString();
        } while (existing != null && existing.contains(key));
        return key;
    }

    /** Gera uma chave com o comprimento informado, sem verificação de unicidade. */
    public static String generate(int length) {
        return generate(length, null);
    }
}
