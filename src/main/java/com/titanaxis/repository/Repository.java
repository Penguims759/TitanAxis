package com.titanaxis.repository;

import com.titanaxis.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    /**
     * Salva uma entidade. A implementação deve decidir se requer auditoria.
     * @param entity A entidade a ser salva.
     * @return A entidade salva.
     */
    T save(T entity);

    /**
     * Salva uma entidade registando a ação na auditoria.
     * @param entity A entidade a ser salva.
     * @param usuarioLogado O utilizador que está a realizar a operação.
     * @return A entidade salva.
     */
    default T save(T entity, Usuario usuarioLogado) {
        // Por defeito, chama o método sem auditoria.
        // Repositórios auditáveis devem sobrepor este método.
        return save(entity);
    }

    Optional<T> findById(ID id);

    List<T> findAll();

    /**
     * Elimina uma entidade pelo seu ID.
     * @param id O ID da entidade a ser eliminada.
     */
    void deleteById(ID id);

    /**
     * Elimina uma entidade pelo seu ID, registando a ação na auditoria.
     * @param id O ID da entidade a ser eliminada.
     * @param usuarioLogado O utilizador que está a realizar a operação.
     */
    default void deleteById(ID id, Usuario usuarioLogado) {
        // Por defeito, chama o método sem auditoria.
        // Repositórios auditáveis devem sobrepor este método.
        deleteById(id);
    }
}