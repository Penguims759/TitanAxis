package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.util.I18n; // Importado
import java.util.List;
import java.util.Optional;

public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final TransactionService transactionService;

    @Inject
    public CategoriaService(CategoriaRepository categoriaRepository, TransactionService transactionService) {
        this.categoriaRepository = categoriaRepository;
        this.transactionService = transactionService;
    }

    public List<Categoria> listarTodasCategorias() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findAllWithProductCount(em)
        );
    }

    public List<Categoria> buscarCategoriasPorNome(String termo) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findByNomeContainingWithProductCount(termo, em)
        );
    }

    public void salvar(Categoria categoria, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, NomeDuplicadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        try {
            transactionService.executeInTransaction(em -> {
                Optional<Categoria> catExistenteOpt = categoriaRepository.findByNome(categoria.getNome(), em);
                if (catExistenteOpt.isPresent() && catExistenteOpt.get().getId() != categoria.getId()) {
                    throw new RuntimeException(I18n.getString("service.category.error.nameExists", categoria.getNome())); 
                }
                categoriaRepository.save(categoria, usuarioLogado, em);
            });
        } catch (RuntimeException e) {
            if(e.getMessage().contains(I18n.getString("service.category.error.nameExists.check"))){ 
                throw new NomeDuplicadoException(e.getMessage());
            }
            throw new PersistenciaException(e.getMessage(), e);
        }
    }

    public void deletar(int id, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        transactionService.executeInTransaction(em ->
                categoriaRepository.deleteById(id, usuarioLogado, em)
        );
    }
}