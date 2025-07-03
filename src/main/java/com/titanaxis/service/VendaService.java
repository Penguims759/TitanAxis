package com.titanaxis.service;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    public VendaService(VendaRepository vendaRepository, ProdutoRepository produtoRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    /**
     * Finaliza uma venda, garantindo que todas as operações (abate de stock, criação de registos)
     * são executadas dentro de uma única transação atómica.
     * @param venda O objeto Venda a ser salvo, já com os seus itens.
     * @param ator O utilizador que realiza a operação.
     * @return A entidade Venda persistida.
     * @throws Exception se a operação falhar.
     */
    public Venda finalizarVenda(Venda venda, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new Exception("A venda não contém itens.");
        }

        // Envolve toda a lógica de negócio numa única transação.
        return transactionService.executeInTransaction(em -> {

            // 1. Validar e abater o stock de cada lote
            for (VendaItem item : venda.getItens()) {
                // Usamos em.find() para obter a versão mais atual do lote da base de dados
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    // Lançar uma exceção aqui irá automaticamente causar o rollback da transação.
                    throw new RuntimeException("Stock insuficiente para o produto: " + item.getLote().getProduto().getNome());
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
                // Não é preciso chamar um 'save' aqui; o Hibernate gere a atualização no commit.
            }

            // 2. Chamar o repositório para salvar a venda e os seus registos associados.
            // O repositório irá usar o mesmo EntityManager desta transação.
            return vendaRepository.save(venda, ator, em);
        });
    }
}