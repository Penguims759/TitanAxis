package com.titanaxis.service;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import java.util.List;
import java.util.Optional;

public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final TransactionService transactionService;

    public CategoriaService(CategoriaRepository categoriaRepository, TransactionService transactionService) {
        this.categoriaRepository = categoriaRepository;
        this.transactionService = transactionService;
    }

    // ALTERADO: Adicionada a declaração "throws"
    public List<Categoria> listarTodasCategorias() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findAllWithProductCount(em)
        );
    }

    // ALTERADO: Adicionada a declaração "throws"
    public List<Categoria> buscarCategoriasPorNome(String termo) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findByNomeContainingWithProductCount(termo, em)
        );
    }

    public void salvar(Categoria categoria, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, NomeDuplicadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        try {
            transactionService.executeInTransaction(em -> {
                Optional<Categoria> catExistenteOpt = categoriaRepository.findByNome(categoria.getNome(), em);
                if (catExistenteOpt.isPresent() && catExistenteOpt.get().getId() != categoria.getId()) {
                    throw new RuntimeException("Já existe uma categoria com este nome.");
                }
                categoriaRepository.save(categoria, usuarioLogado, em);
            });
        } catch (RuntimeException e) {
            throw new NomeDuplicadoException(e.getMessage());
        }
    }

    public void deletar(int id, Usuario usuarioLogado) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (usuarioLogado == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em ->
                categoriaRepository.deleteById(id, usuarioLogado, em)
        );
    }
}