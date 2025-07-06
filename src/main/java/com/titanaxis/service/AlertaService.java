// src/main/java/com/titanaxis/service/AlertaService.java
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
import java.util.List;
import java.util.logging.Logger;
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
     * NOVO: Retorna uma lista de produtos que estão com estoque baixo.
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
     * NOVO: Retorna uma lista de lotes que estão próximos do vencimento.
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
        logger.info("Iniciando geração de mensagens de alerta...");
        List<String> mensagens = new ArrayList<>();

        // 1. Alerta de Estoque Baixo
        List<Produto> comEstoqueBaixo = getProdutosComEstoqueBaixo();
        if (!comEstoqueBaixo.isEmpty()) {
            mensagens.add("--- ALERTA: ESTOQUE BAIXO ---");
            comEstoqueBaixo.forEach(p -> mensagens.add("Produto: " + p.getNome() + " - Quantidade Total: " + p.getQuantidadeTotal()));
        }

        // 2. Alertas de Vencimento
        List<Lote> proximosVencimento = getLotesProximosDoVencimento();
        if (!proximosVencimento.isEmpty()) {
            if (!mensagens.isEmpty()) mensagens.add("");
            mensagens.add("--- ALERTA: LOTES PRÓXIMOS AO VENCIMENTO ---");
            proximosVencimento.forEach(lote -> mensagens.add("Produto: " + lote.getProduto().getNome() + " (Lote: " + lote.getNumeroLote() + ") - Vence em: " + lote.getDataValidade().format(DATE_FORMATTER)));
        }

        // Adicione aqui a lógica para lotes já vencidos se desejar

        if (mensagens.isEmpty()) {
            mensagens.add("Nenhum alerta de estoque ativo.");
        }

        logger.info("Geração de alertas concluída.");
        return mensagens;
    }
}