package com.vipkeys.util;

import java.util.Locale;

/**
 * Converte durações em texto (ex.: {@code 7d}, {@code 30d}, {@code 1h}, {@code perm})
 * para milissegundos.
 */
public final class DurationParser {

    /** Valor especial que representa VIP permanente. */
    public static final long PERMANENT = -1L;

    private DurationParser() {
    }

    /**
     * Converte uma string de duração para milissegundos.
     *
     * <p>Formatos aceitos (case-insensitive):</p>
     * <ul>
     *   <li>{@code perm}, {@code permanent}, {@code permanente} — permanente (retorna {@link #PERMANENT});</li>
     *   <li>{@code <número>d} — dias;</li>
     *   <li>{@code <número>h} — horas;</li>
     *   <li>{@code <número>m} — minutos;</li>
     *   <li>{@code <número>s} — segundos.</li>
     * </ul>
     *
     * @param input a duração em texto
     * @return milissegundos, ou {@link #PERMANENT} para permanente
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static long parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Duração vazia.");
        }
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Duração vazia.");
        }
        if (s.equals("perm") || s.equals("permanent") || s.equals("permanente")) {
            return PERMANENT;
        }

        int i = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            i++;
        }
        if (i == 0) {
            throw new IllegalArgumentException("Duração inválida: " + input);
        }
        long value = Long.parseLong(s.substring(0, i));
        if (value <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero: " + input);
        }

        String unit = s.substring(i);
        long multiplier;
        switch (unit) {
            case "d":
            case "day":
            case "days":
                multiplier = 86_400_000L;
                break;
            case "h":
            case "hour":
            case "hours":
                multiplier = 3_600_000L;
                break;
            case "m":
            case "min":
            case "minute":
            case "minutes":
                multiplier = 60_000L;
                break;
            case "s":
            case "sec":
            case "second":
            case "seconds":
                multiplier = 1_000L;
                break;
            default:
                throw new IllegalArgumentException("Unidade de duração inválida: " + input
                        + " (use d, h, m, s ou perm)");
        }
        return value * multiplier;
    }
}
