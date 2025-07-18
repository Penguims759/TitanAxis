package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.model.VendaStatus;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.I18n;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;
    private final FinanceiroService financeiroService; // NOVA DEPENDÊNCIA

    @Inject
    public VendaService(VendaRepository vendaRepository, TransactionService transactionService, FinanceiroService financeiroService) { // ATUALIZADO
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
        this.financeiroService = financeiroService; // ATUALIZADO
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

    public Venda salvarOrcamento(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException, PersistenciaException {
        validarVenda(venda, ator);
        venda.setStatus(VendaStatus.ORCAMENTO);
        return transactionService.executeInTransactionWithResult(em ->
                vendaRepository.save(venda, ator, em)
        );
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException, PersistenciaException {
        validarVenda(venda, ator);
        venda.setStatus(VendaStatus.FINALIZADA);

        return transactionService.executeInTransactionWithResult(em -> {
            for (VendaItem item : venda.getItens()) {
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    throw new RuntimeException(I18n.getString("service.sale.error.insufficientStock", item.getLote().getProduto().getNome()));
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
            }

            if (venda.getCliente() != null && venda.getCreditoUtilizado() > 0) {
                Cliente cliente = em.find(Cliente.class, venda.getCliente().getId());
                cliente.debitarCredito(venda.getCreditoUtilizado());
                em.merge(cliente);
            }

            // Salva a venda primeiro para obter o ID
            Venda vendaSalva = vendaRepository.save(venda, ator, em);

            // Gera as contas a receber se necessário
            financeiroService.gerarContasAReceberParaVenda(vendaSalva, em);

            return vendaSalva;
        });
    }


    public Venda converterOrcamentoEmVenda(int orcamentoId, Usuario ator) throws PersistenciaException, UtilizadorNaoAutenticadoException, CarrinhoVazioException {
        Venda orcamento = buscarVendaCompletaPorId(orcamentoId)
                .orElseThrow(() -> new PersistenciaException(I18n.getString("service.sale.error.quoteNotFound"), null));

        if (orcamento.getStatus() != VendaStatus.ORCAMENTO) {
            throw new IllegalStateException(I18n.getString("service.sale.error.onlyQuotesCanBeConverted"));
        }
        return finalizarVenda(orcamento, ator);
    }

    public void cancelarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated"));
        }

        if (venda.getItens() != null && !venda.getItens().isEmpty()) {
            venda.setStatus(VendaStatus.CANCELADA);
            transactionService.executeInTransaction(em -> {
                vendaRepository.save(venda, ator, em);
            });
        }
    }

    private void validarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.sale.error.notAuthenticatedForSale"));
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new CarrinhoVazioException(I18n.getString("service.sale.error.emptyCart"));
        }
    }
}