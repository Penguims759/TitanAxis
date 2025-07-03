package com.titanaxis.service;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;

    public ProdutoService(ProdutoRepository produtoRepository, TransactionService transactionService) {
        this.produtoRepository = produtoRepository;
        this.transactionService = transactionService;
    }

    // --- Métodos de Leitura (não precisam de transação) ---

    public List<Produto> listarProdutos(boolean incluirInativos) {
        return incluirInativos ? produtoRepository.findAllIncludingInactive() : produtoRepository.findAll();
    }

    public List<Produto> listarProdutosAtivosParaVenda() {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getQuantidadeTotal() > 0)
                .collect(Collectors.toList());
    }

    public List<Lote> buscarLotesDisponiveis(int produtoId) {
        return produtoRepository.findLotesByProdutoId(produtoId).stream()
                .filter(l -> l.getQuantidade() > 0)
                .collect(Collectors.toList());
    }

    public Optional<Produto> buscarProdutoPorId(int id) {
        return produtoRepository.findById(id);
    }

    public List<Lote> buscarLotesPorProdutoId(int produtoId) {
        return produtoRepository.findLotesByProdutoId(produtoId);
    }

    // CORREÇÃO APLICADA AQUI
    public Optional<Lote> buscarLotePorId(int loteId) {
        return produtoRepository.findLoteById(loteId); // Chamava o método errado findById() do produto
    }

    // --- Métodos de Escrita (agora usam o TransactionService) ---

    public Produto salvarProduto(Produto produto, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        return transactionService.executeInTransaction(em -> {
            return produtoRepository.save(produto, ator, em);
        });
    }

    public Lote salvarLote(Lote lote, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        return transactionService.executeInTransaction(em -> {
            return produtoRepository.saveLote(lote, ator, em);
        });
    }

    public void removerLote(int loteId, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em -> {
            produtoRepository.deleteLoteById(loteId, ator, em);
        });
    }

    public void alterarStatusProduto(int produtoId, boolean novoStatus, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em -> {
            produtoRepository.updateStatusAtivo(produtoId, novoStatus, ator, em);
        });
    }
}