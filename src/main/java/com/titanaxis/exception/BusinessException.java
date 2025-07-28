package com.titanaxis.exception;

/**
 * Exceção base para erros de regra de negócio.
 * Todas as exceções específicas de negócio devem herdar desta classe.
 */
public abstract class BusinessException extends Exception {
    private final String errorCode;
    private final Object[] parameters;

    public BusinessException(String message) {
        this(message, null, null);
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public BusinessException(String message, String errorCode, Object[] parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public BusinessException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public BusinessException(String message, String errorCode, Object[] parameters, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Retorna uma mensagem formatada com os parâmetros, se disponíveis.
     */
    public String getFormattedMessage() {
        if (parameters != null && parameters.length > 0) {
            return String.format(getMessage(), parameters);
        }
        return getMessage();
    }
}