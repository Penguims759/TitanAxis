package com.titanaxis.exception;

/**
 * Exceção lançada quando uma entidade não é encontrada.
 */
public class EntityNotFoundException extends BusinessException {
    private final Class<?> entityType;
    private final Object entityId;

    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND");
        this.entityType = null;
        this.entityId = null;
    }

    public EntityNotFoundException(Class<?> entityType, Object entityId) {
        super(String.format("%s com ID %s não encontrado", 
            entityType.getSimpleName(), entityId), "ENTITY_NOT_FOUND");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s com ID %s não encontrado", entityName, entityId), "ENTITY_NOT_FOUND");
        this.entityType = null;
        this.entityId = entityId;
    }

    public EntityNotFoundException(Class<?> entityType, String fieldName, Object fieldValue) {
        super(String.format("%s com %s '%s' não encontrado", 
            entityType.getSimpleName(), fieldName, fieldValue), "ENTITY_NOT_FOUND");
        this.entityType = entityType;
        this.entityId = fieldValue;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
}