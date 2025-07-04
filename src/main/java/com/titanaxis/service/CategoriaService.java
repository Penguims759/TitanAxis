package com.titanaxis.service;

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

    public List<Categoria> listarTodasCategorias() {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findAllWithProductCount(em)
        );
    }

    public List<Categoria> buscarCategoriasPorNome(String termo) {
        return transactionService.executeInTransactionWithResult(em ->
                categoriaRepository.findByNomeContainingWithProductCount(termo, em)
        );
    }

    public void salvar(Categoria categoria, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");

        try {
            transactionService.executeInTransaction(em -> {
                Optional<Categoria> catExistenteOpt = categoriaRepository.findByNome(categoria.getNome(), em);
                if (catExistenteOpt.isPresent() && catExistenteOpt.get().getId() != categoria.getId()) {
                    throw new RuntimeException("Já existe uma categoria com este nome.");
                }
                categoriaRepository.save(categoria, usuarioLogado, em);
            });
        } catch (RuntimeException e) {
            // Captura a exceção da transação e a relança como uma exceção verificada para o presenter
            throw new Exception(e.getMessage());
        }
    }

    public void deletar(int id, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        transactionService.executeInTransaction(em ->
                categoriaRepository.deleteById(id, usuarioLogado, em)
        );
    }
}