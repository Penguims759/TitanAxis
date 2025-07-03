package com.titanaxis.service;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    // O construtor estava a pedir um ProdutoRepository que já não é necessário aqui.
    public VendaService(VendaRepository vendaRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new Exception("A venda não contém itens.");
        }

        // CORREÇÃO: Usamos o método com o nome explícito que retorna um resultado.
        return transactionService.executeInTransactionWithResult(em -> {

            for (VendaItem item : venda.getItens()) {
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    throw new RuntimeException("Stock insuficiente para o produto: " + item.getLote().getProduto().getNome());
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
            }

            return vendaRepository.save(venda, ator, em);
        });
    }
}