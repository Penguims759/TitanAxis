// src/main/java/com/titanaxis/service/VendaService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.model.VendaStatus; // IMPORTADO
import com.titanaxis.repository.VendaRepository;
import java.util.List;
import java.util.Optional;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    @Inject
    public VendaService(VendaRepository vendaRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    public List<Venda> listarTodasAsVendas() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(vendaRepository::findAll);
    }

    public Optional<Venda> buscarVendaCompletaPorId(int id) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findById(id, em)
        );
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new CarrinhoVazioException("A venda não contém itens.");
        }

        // Define o status como FINALIZADA antes de salvar
        venda.setStatus(VendaStatus.FINALIZADA);

        return transactionService.executeInTransactionWithResult(em -> {
            for (VendaItem item : venda.getItens()) {
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    throw new RuntimeException("Estoque insuficiente para o produto: " + item.getLote().getProduto().getNome());
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
            }
            return vendaRepository.save(venda, ator, em);
        });
    }

    // NOVO MÉTODO
    public void cancelarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar a operação.");
        }

        // Apenas salva a venda se ela já tiver itens, caso contrário, não há o que cancelar.
        if (venda.getItens() != null && !venda.getItens().isEmpty()) {
            venda.setStatus(VendaStatus.CANCELADA);
            transactionService.executeInTransaction(em -> {
                vendaRepository.save(venda, ator, em);
            });
        }
    }
}