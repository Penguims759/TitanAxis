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
import com.titanaxis.model.VendaStatus;
import com.titanaxis.repository.VendaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<Venda> buscarVendasPorFiltro(LocalDate dataInicio, LocalDate dataFim, VendaStatus status, String clienteNome) throws PersistenciaException {
        LocalDateTime inicio = (dataInicio != null) ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = (dataFim != null) ? dataFim.atTime(LocalTime.MAX) : null;
        return transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findWithFilters(inicio, fim, status, clienteNome, em)
        );
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new CarrinhoVazioException("O carrinho está vazio.");
        }

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

    public void cancelarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }

        if (venda.getItens() != null && !venda.getItens().isEmpty()) {
            venda.setStatus(VendaStatus.CANCELADA);
            transactionService.executeInTransaction(em -> {
                // Apenas salva a venda para registar o cancelamento
                vendaRepository.save(venda, ator, em);
            });
        }
        // Se a venda não tiver itens, não há necessidade de a persistir.
    }
}