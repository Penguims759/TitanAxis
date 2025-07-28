package com.titanaxis.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exceção para erros de validação de dados.
 */
public class ValidationException extends BusinessException {
    private final List<ValidationError> validationErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = new ArrayList<>();
    }

    public ValidationException(String message, List<ValidationError> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    public ValidationException(String field, String message) {
        super("Erro de validação no campo: " + field, "FIELD_VALIDATION_ERROR");
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(new ValidationError(field, message));
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void addValidationError(String field, String message) {
        this.validationErrors.add(new ValidationError(field, message));
    }

    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }

    @Override
    public String getFormattedMessage() {
        if (validationErrors.isEmpty()) {
            return super.getFormattedMessage();
        }

        StringBuilder sb = new StringBuilder(super.getFormattedMessage());
        sb.append("\nDetalhes dos erros:");
        for (ValidationError error : validationErrors) {
            sb.append(String.format("\n- %s: %s", error.getField(), error.getMessage()));
        }
        return sb.toString();
    }

    /**
     * Classe interna para representar um erro de validação específico.
     */
    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s'}", field, message);
        }
    }
}