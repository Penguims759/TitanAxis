// src/main/java/com/titanaxis/service/AnalyticsService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    @Inject
    public AnalyticsService(VendaRepository vendaRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    /**
     * Analisa as vendas recentes para encontrar o produto mais vendido.
     * @return O nome do produto mais vendido ou uma mensagem se não houver vendas.
     * @throws PersistenciaException se ocorrer um erro na base de dados.
     */
    public String getTopSellingProduct() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            List<VendaItem> recentItems = vendaRepository.findAllItems(em);

            if (recentItems.isEmpty()) {
                return "Nenhuma venda registrada ainda.";
            }

            Map<String, Integer> productSales = recentItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getProduto().getNome(),
                            Collectors.summingInt(VendaItem::getQuantidade)
                    ));

            return productSales.entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse("Não foi possível determinar o produto mais vendido.");
        });
    }

    /**
     * Gera um resumo proativo com os insights mais importantes para o usuário ao fazer login.
     * @return Uma string com o resumo ou uma string vazia se não houver nada importante.
     */
    public String getProactiveInsightsSummary() {
        try {
            String topProduct = getTopSellingProduct();
            if (topProduct.contains("Nenhuma venda")) {
                return "";
            }
            return "Só para você saber, o produto mais vendido até agora é a(o) '" + topProduct + "'. Mantenha o estoque em dia!";
        } catch (PersistenciaException e) {
            return "Não consegui carregar os insights diários devido a um erro no banco de dados.";
        }
    }
}