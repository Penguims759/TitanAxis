// src/main/java/com/titanaxis/service/AlertaService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import java.util.stream.Collectors;

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

    /**
     * Retorna uma lista de produtos que estão com estoque baixo.
     */
    public List<Produto> getProdutosComEstoqueBaixo() throws PersistenciaException {
        List<Produto> todosProdutos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );
        return todosProdutos.stream()
                .filter(p -> p.isAtivo() && p.getQuantidadeTotal() > 0 && p.getQuantidadeTotal() <= LIMITE_ESTOQUE_BAIXO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna uma lista de lotes que estão próximos do vencimento.
     */
    public List<Lote> getLotesProximosDoVencimento() throws PersistenciaException {
        List<Produto> todosProdutos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );
        LocalDate dataLimite = LocalDate.now().plusDays(DIAS_PARA_VENCIMENTO_ALERTA);
        List<Lote> lotesProximos = new ArrayList<>();
        for (Produto p : todosProdutos) {
            if (p.isAtivo()) {
                p.getLotes().stream()
                        .filter(lote -> lote.getQuantidade() > 0 && lote.getDataValidade() != null && !lote.getDataValidade().isBefore(LocalDate.now()) && lote.getDataValidade().isBefore(dataLimite.plusDays(1)))
                        .forEach(lotesProximos::add);
            }
        }
        return lotesProximos;
    }

    /**
     * Gera uma lista de strings formatadas com todos os alertas ativos.
     * @return Lista de mensagens de alerta.
     */
    public List<String> gerarMensagensDeAlerta() throws PersistenciaException {
        logger.info(I18n.getString("log.generatingAlerts")); 
        List<String> mensagens = new ArrayList<>();

        // 1. Alerta de Estoque Baixo
        List<Produto> comEstoqueBaixo = getProdutosComEstoqueBaixo();
        if (!comEstoqueBaixo.isEmpty()) {
            mensagens.add(I18n.getString("alert.service.lowStock.header")); 
            comEstoqueBaixo.forEach(p -> mensagens.add(I18n.getString("alert.service.lowStock.line", p.getNome(), p.getQuantidadeTotal()))); 
        }

        // 2. Alertas de Vencimento
        List<Lote> proximosVencimento = getLotesProximosDoVencimento();
        if (!proximosVencimento.isEmpty()) {
            if (!mensagens.isEmpty()) mensagens.add("");
            mensagens.add(I18n.getString("alert.service.expiry.header")); 
            proximosVencimento.forEach(lote -> mensagens.add(I18n.getString("alert.service.expiry.line", lote.getProduto().getNome(), lote.getNumeroLote(), lote.getDataValidade().format(DATE_FORMATTER)))); 
        }

        if (mensagens.isEmpty()) {
            mensagens.add(I18n.getString("alert.panel.noAlerts")); // Reaproveitando chave
        }

        logger.info(I18n.getString("log.alertsGenerated")); 
        return mensagens;
    }
}