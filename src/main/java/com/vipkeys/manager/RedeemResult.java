package com.vipkeys.manager;

/**
 * Resultado da tentativa de resgate de uma chave.
 */
public enum RedeemResult {

    SUCCESS("Chave resgatada com sucesso!"),
    INVALID("Chave inválida."),
    ALREADY_USED("Chave já usada."),
    EXPIRED("Chave expirada."),
    NO_LUCKPERMS("LuckPerms não está disponível no servidor. Contate um administrador."),
    NO_ECONOMY("LlamaEconomy não está disponível para creditar o balance deste kit."),
    GROUP_NOT_FOUND("Grupo não encontrado no LuckPerms."),
    KIT_NOT_FOUND("Kit associado à chave não encontrado."),
    ERROR("Ocorreu um erro ao resgatar a chave.");

    private final String message;

    RedeemResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
