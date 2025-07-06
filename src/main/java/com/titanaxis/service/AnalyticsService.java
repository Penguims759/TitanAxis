// src/main/java/com/titanaxis/service/AnalyticsService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));


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

    // NOVO MÉTODO
    public String getTopBuyingClients(int limit) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(vendaRepository::findAll);
        if (vendas.isEmpty()) {
            return "Nenhuma venda registrada para analisar os clientes.";
        }

        Map<String, Double> clientSpending = vendas.stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getCliente().getNome(),
                        Collectors.summingDouble(Venda::getValorTotal)
                ));

        if (clientSpending.isEmpty()) {
            return "Nenhum cliente associado às vendas foi encontrado.";
        }

        String topClients = clientSpending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> String.format("- %s (%s)", entry.getKey(), CURRENCY_FORMAT.format(entry.getValue())))
                .collect(Collectors.joining("\n"));

        return "Os " + limit + " clientes que mais compraram foram:\n" + topClients;
    }

    // NOVO MÉTODO
    public String getClientPurchaseHistory(int clienteId) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasByClienteId(clienteId, em)
        );

        if (vendas.isEmpty()) {
            return "Este cliente ainda não realizou nenhuma compra.";
        }

        StringBuilder history = new StringBuilder("Histórico de compras:\n");
        for (Venda venda : vendas) {
            history.append(String.format("  Venda #%d (%s) - Total: %s\n",
                    venda.getId(),
                    venda.getDataVenda().format(DATE_FORMATTER),
                    CURRENCY_FORMAT.format(venda.getValorTotal())
            ));
            for (VendaItem item : venda.getItens()) {
                history.append(String.format("    - %dx %s\n",
                        item.getQuantidade(),
                        item.getProduto().getNome()
                ));
            }
        }
        return history.toString();
    }
}