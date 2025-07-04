// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/service/AlertaService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.AppLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class AlertaService {
    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;
    private static final int LIMITE_ESTOQUE_BAIXO = 10;
    private static final int DIAS_PARA_VENCIMENTO_ALERTA = 30;
    private static final Logger logger = AppLogger.getLogger();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    public AlertaService(ProdutoRepository produtoRepository, TransactionService transactionService) {
        this.produtoRepository = produtoRepository;
        this.transactionService = transactionService;
    }

    public List<String> gerarMensagensDeAlerta() throws PersistenciaException {
        logger.info("Iniciando geração de mensagens de alerta...");
        List<String> mensagens = new ArrayList<>();

        List<Produto> todosProdutos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );

        logger.info(todosProdutos.size() + " produtos carregados para verificação.");

        // 1. Alerta de Estoque Baixo
        List<Produto> comEstoqueBaixo = new ArrayList<>();
        for (Produto p : todosProdutos) {
            if (p.getQuantidadeTotal() > 0 && p.getQuantidadeTotal() <= LIMITE_ESTOQUE_BAIXO) {
                comEstoqueBaixo.add(p);
            }
        }
        if (!comEstoqueBaixo.isEmpty()) {
            mensagens.add("--- ALERTA: ESTOQUE BAIXO ---");
            comEstoqueBaixo.forEach(p -> mensagens.add("Produto: " + p.getNome() + " - Quantidade Total: " + p.getQuantidadeTotal()));
        }

        // 2. Alertas de Vencimento
        LocalDate hoje = LocalDate.now();
        LocalDate dataLimite = hoje.plusDays(DIAS_PARA_VENCIMENTO_ALERTA);
        List<String> alertasProximosVencimento = new ArrayList<>();
        List<String> alertasVencidos = new ArrayList<>();

        for (Produto p : todosProdutos) {
            for (Lote lote : p.getLotes()) {
                if (lote.getDataValidade() != null && lote.getQuantidade() > 0) {
                    if (lote.getDataValidade().isBefore(hoje)) {
                        alertasVencidos.add("Produto: " + p.getNome() + " (Lote: " + lote.getNumeroLote() + ") - VENCEU EM: " + lote.getDataValidade().format(DATE_FORMATTER));
                    } else if (lote.getDataValidade().isBefore(dataLimite.plusDays(1))) {
                        alertasProximosVencimento.add("Produto: " + p.getNome() + " (Lote: " + lote.getNumeroLote() + ") - Vence em: " + lote.getDataValidade().format(DATE_FORMATTER));
                    }
                }
            }
        }

        if (!alertasProximosVencimento.isEmpty()) {
            if (!mensagens.isEmpty()) mensagens.add("");
            mensagens.add("--- ALERTA: LOTES PRÓXIMOS AO VENCIMENTO ---");
            mensagens.addAll(alertasProximosVencimento);
        }

        if (!alertasVencidos.isEmpty()) {
            if (!mensagens.isEmpty()) mensagens.add("");
            mensagens.add("--- ALERTA: LOTES VENCIDOS ---");
            mensagens.addAll(alertasVencidos);
        }

        if (mensagens.isEmpty()) {
            mensagens.add("Nenhum alerta de estoque ativo.");
        }

        logger.info("Geração de alertas concluída.");
        return mensagens;
    }
}