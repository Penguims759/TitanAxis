package com.titanaxis.service;

import com.titanaxis.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Serviço para validação de objetos usando Bean Validation.
 */
public class ValidationService {
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    /**
     * Valida um objeto e lança ValidationException se houver erros.
     */
    public static <T> void validate(T object) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        
        if (!violations.isEmpty()) {
            List<ValidationException.ValidationError> errors = new ArrayList<>();
            
            for (ConstraintViolation<T> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.add(new ValidationException.ValidationError(fieldName, message));
            }
            
            throw new ValidationException("Erro de validação", errors);
        }
    }

    /**
     * Valida um objeto e retorna a lista de erros sem lançar exceção.
     */
    public static <T> List<ValidationException.ValidationError> validateAndGetErrors(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        
        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.add(new ValidationException.ValidationError(fieldName, message));
        }
        
        return errors;
    }

    /**
     * Verifica se um objeto é válido.
     */
    public static <T> boolean isValid(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        return violations.isEmpty();
    }

    /**
     * Valida propriedades específicas de um objeto.
     */
    public static <T> void validateProperty(T object, String propertyName) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName);
        
        if (!violations.isEmpty()) {
            List<ValidationException.ValidationError> errors = new ArrayList<>();
            
            for (ConstraintViolation<T> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.add(new ValidationException.ValidationError(fieldName, message));
            }
            
            throw new ValidationException("Erro de validação na propriedade: " + propertyName, errors);
        }
    }

    /**
     * Valida um valor para uma propriedade específica.
     */
    public static <T> void validateValue(Class<T> beanType, String propertyName, Object value) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validateValue(beanType, propertyName, value);
        
        if (!violations.isEmpty()) {
            List<ValidationException.ValidationError> errors = new ArrayList<>();
            
            for (ConstraintViolation<T> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.add(new ValidationException.ValidationError(fieldName, message));
            }
            
            throw new ValidationException("Erro de validação no valor da propriedade: " + propertyName, errors);
        }
    }

    /**
     * Fecha o ValidatorFactory. Deve ser chamado no shutdown da aplicação.
     */
    public static void close() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }
}