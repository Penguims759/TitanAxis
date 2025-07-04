package com.titanaxis.exception;

/**
 * Representa um erro inesperado durante uma operação na base de dados.
 * É usada para encapsular exceções da camada de persistência (JPA/Hibernate)
 * e apresentá-las de forma controlada às camadas superiores.
 */
public class PersistenciaException extends Exception {
    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}