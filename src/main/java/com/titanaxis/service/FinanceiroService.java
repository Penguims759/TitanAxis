package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.*;
import com.titanaxis.repository.FinanceiroRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;
    private final TransactionService transactionService;

    @Inject
    public FinanceiroService(FinanceiroRepository financeiroRepository, TransactionService transactionService) {
        this.financeiroRepository = financeiroRepository;
        this.transactionService = transactionService;
    }

    // --- Contas a Receber ---
    public List<ContasAReceber> listarContasAReceber(boolean apenasPendentes) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                financeiroRepository.findContasAReceber(apenasPendentes, em)
        );
    }

    public void registrarPagamento(int contaId) throws PersistenciaException {
        transactionService.executeInTransaction(em -> {
            financeiroRepository.findContaAReceberById(contaId, em).ifPresent(conta -> {
                conta.setStatus("Pago");
                conta.setDataPagamento(LocalDate.now());
                financeiroRepository.saveContaAReceber(conta, em);
            });
        });
    }

    public void gerarContasAReceberParaVenda(Venda venda, jakarta.persistence.EntityManager em) {
        if (!"A Prazo".equalsIgnoreCase(venda.getFormaPagamento()) || venda.getNumeroParcelas() <= 0) {
            return;
        }

        double valorParcela = venda.getValorTotal() / venda.getNumeroParcelas();
        LocalDate dataVencimento = LocalDate.now();

        for (int i = 1; i <= venda.getNumeroParcelas(); i++) {
            dataVencimento = dataVencimento.plusMonths(1);

            ContasAReceber conta = new ContasAReceber();
            conta.setVenda(venda);
            conta.setNumeroParcela(i);
            conta.setValorParcela(valorParcela);
            conta.setDataVencimento(dataVencimento);
            conta.setStatus("Pendente");

            financeiroRepository.saveContaAReceber(conta, em);
        }
    }


    // --- Metas ---
    public List<MetaVenda> listarMetas() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(financeiroRepository::findAllMetas);
    }

    public Optional<MetaVenda> findMetaById(int id) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> financeiroRepository.findById(id, em));
    }

    public void salvarMeta(MetaVenda meta) throws PersistenciaException {
        transactionService.executeInTransaction(em -> {
            financeiroRepository.saveMeta(meta, em);
        });
    }

    public void deletarMeta(int metaId) throws PersistenciaException {
        transactionService.executeInTransaction(em -> financeiroRepository.deleteMetaById(metaId, em));
    }
}