// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/service/VendaService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    @Inject
    public VendaService(VendaRepository vendaRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException, Exception {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new Exception("A venda não contém itens.");
        }

        try {
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
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
    }
}