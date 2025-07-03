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

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

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

    public Optional<Lote> buscarLotePorId(int loteId) {
        return produtoRepository.findLoteById(loteId);
    }

    public void salvarProduto(Produto produto, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        produtoRepository.save(produto, ator);
    }

    /**
     * Salva um lote e retorna a instância persistida (com ID).
     * @param lote O lote a ser salvo.
     * @param ator O utilizador que realiza a ação.
     * @return O lote salvo.
     * @throws Exception se o utilizador não estiver autenticado.
     */
    public Lote salvarLote(Lote lote, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        // CORREÇÃO: Adicionamos o 'return' para devolver o lote que o repositório persistiu.
        return produtoRepository.saveLote(lote, ator);
    }

    public void removerLote(int loteId, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        produtoRepository.deleteLoteById(loteId, ator);
    }

    public void alterarStatusProduto(int produtoId, boolean novoStatus, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }
        produtoRepository.updateStatusAtivo(produtoId, novoStatus, ator);
    }
}