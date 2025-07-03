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

    public List<Categoria> listarTodasCategorias() { return categoriaRepository.findAllWithProductCount(); }
    public List<Categoria> buscarCategoriasPorNome(String termo) { return categoriaRepository.findByNomeContainingWithProductCount(termo); }

    public void salvar(Categoria categoria, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        Optional<Categoria> catExistenteOpt = categoriaRepository.findByNome(categoria.getNome());
        if (catExistenteOpt.isPresent() && catExistenteOpt.get().getId() != categoria.getId()) {
            throw new Exception("Já existe uma categoria com este nome.");
        }
        transactionService.executeInTransaction(em -> categoriaRepository.save(categoria, usuarioLogado, em));
    }

    public void deletar(int id, Usuario usuarioLogado) throws Exception {
        if (usuarioLogado == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        transactionService.executeInTransaction(em -> categoriaRepository.deleteById(id, usuarioLogado, em));
    }
}