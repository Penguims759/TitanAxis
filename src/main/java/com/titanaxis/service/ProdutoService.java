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

    public List<Produto> listarProdutos(boolean incluirInativos) {
        return transactionService.executeInTransactionWithResult(em -> {
            if (incluirInativos) {
                return produtoRepository.findAllIncludingInactive(em);
            }
            return produtoRepository.findAll(em);
        });
    }

    public List<Produto> listarProdutosAtivosParaVenda() {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAll(em).stream()
                        .filter(p -> p.getQuantidadeTotal() > 0)
                        .collect(Collectors.toList())
        );
    }

    public List<Lote> buscarLotesDisponiveis(int produtoId) {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findLotesByProdutoId(produtoId, em).stream()
                        .filter(l -> l.getQuantidade() > 0)
                        .collect(Collectors.toList())
        );
    }

    public Optional<Produto> buscarProdutoPorId(int id) {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findById(id, em)
        );
    }

    public Optional<Lote> buscarLotePorId(int loteId) {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findLoteById(loteId, em)
        );
    }

    public Produto salvarProduto(Produto produto, Usuario ator) throws Exception {
        if (ator == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.save(produto, ator, em)
        );
    }

    public Lote salvarLote(Lote lote, Usuario ator) throws Exception {
        if (ator == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.saveLote(lote, ator, em)
        );
    }

    public void removerLote(int loteId, Usuario ator) throws Exception {
        if (ator == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        transactionService.executeInTransaction(em ->
                produtoRepository.deleteLoteById(loteId, ator, em)
        );
    }

    public void alterarStatusProduto(int produtoId, boolean novoStatus, Usuario ator) throws Exception {
        if (ator == null) throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        transactionService.executeInTransaction(em ->
                produtoRepository.updateStatusAtivo(produtoId, novoStatus, ator, em)
        );
    }
}