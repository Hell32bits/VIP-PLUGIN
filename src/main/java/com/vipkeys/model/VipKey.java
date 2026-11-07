package com.vipkeys.model;

/**
 * Representa uma chave de VIP armazenada pelo plugin.
 *
 * <p>Estrutura de dados:</p>
 * <ul>
 *   <li>{@code code}      — código da chave (ex.: {@code A1B2C3D4E5F6}, 12 caracteres);</li>
 *   <li>{@code group}     — grupo do LuckPerms concedido ao resgatar;</li>
 *   <li>{@code duration}  — duração em formato legível (ex.: {@code 7d}, {@code 1h}, {@code perm});</li>
 *   <li>{@code durationMs}— duração em milissegundos ({@code -1} = permanente);</li>
 *   <li>{@code createdAt} — timestamp de criação (epoch ms);</li>
 *   <li>{@code expiresAt} — timestamp de expiração da própria chave (epoch ms, {@code 0} = nunca expira);</li>
 *   <li>{@code used}      — se a chave já foi usada;</li>
 *   <li>{@code usedBy}    — UUID (String) de quem usou, ou {@code null};</li>
 *   <li>{@code usedAt}    — timestamp de uso (epoch ms, {@code 0} = nunca usada);</li>
 *   <li>{@code kitName}   — nome do kit VIP associado (opcional, {@code null} = sem kit).</li>
 * </ul>
 */
public class VipKey {

    private final String code;
    private String group;
    private String duration;
    private long durationMs;
    private long createdAt;
    private long expiresAt;
    private boolean used;
    private String usedBy;
    private long usedAt;
    private String kitName;

    public VipKey(String code, String group, String duration, long durationMs,
                  long createdAt, long expiresAt, boolean used, String usedBy, long usedAt,
                  String kitName) {
        this.code = code;
        this.group = group;
        this.duration = duration;
        this.durationMs = durationMs;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = used;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
        this.kitName = kitName;
    }

    public String getCode() {
        return code;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(String usedBy) {
        this.usedBy = usedBy;
    }

    public long getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(long usedAt) {
        this.usedAt = usedAt;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    /** Retorna {@code true} se o VIP concedido é permanente. */
    public boolean isPermanent() {
        return durationMs < 0;
    }

    /** Retorna {@code true} se a própria chave expirou (independente do VIP). */
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
}
