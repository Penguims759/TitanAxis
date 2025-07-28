package com.titanaxis.exception;

/**
 * Exceção para falhas de autenticação.
 */
public class AuthenticationException extends BusinessException {
    private final String username;
    private final AuthenticationFailureReason reason;

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
        this.username = null;
        this.reason = AuthenticationFailureReason.UNKNOWN;
    }

    public AuthenticationException(String username, AuthenticationFailureReason reason) {
        super(buildMessage(username, reason), "AUTHENTICATION_FAILED");
        this.username = username;
        this.reason = reason;
    }

    public AuthenticationException(String message, String username, AuthenticationFailureReason reason) {
        super(message, "AUTHENTICATION_FAILED");
        this.username = username;
        this.reason = reason;
    }

    private static String buildMessage(String username, AuthenticationFailureReason reason) {
        switch (reason) {
            case INVALID_CREDENTIALS:
                return "Credenciais inválidas para o usuário: " + username;
            case USER_NOT_FOUND:
                return "Usuário não encontrado: " + username;
            case ACCOUNT_LOCKED:
                return "Conta bloqueada para o usuário: " + username;
            case ACCOUNT_DISABLED:
                return "Conta desabilitada para o usuário: " + username;
            case PASSWORD_EXPIRED:
                return "Senha expirada para o usuário: " + username;
            case TOO_MANY_ATTEMPTS:
                return "Muitas tentativas de login para o usuário: " + username;
            default:
                return "Falha na autenticação para o usuário: " + username;
        }
    }

    public String getUsername() {
        return username;
    }

    public AuthenticationFailureReason getReason() {
        return reason;
    }

    public enum AuthenticationFailureReason {
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        ACCOUNT_LOCKED,
        ACCOUNT_DISABLED,
        PASSWORD_EXPIRED,
        TOO_MANY_ATTEMPTS,
        UNKNOWN
    }
}