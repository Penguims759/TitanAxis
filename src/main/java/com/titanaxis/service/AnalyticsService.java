package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "PT"));

    @Inject
    public AnalyticsService(VendaRepository vendaRepository, ClienteRepository clienteRepository, AuditoriaRepository auditoriaRepository, ProdutoRepository produtoRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.clienteRepository = clienteRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.produtoRepository = produtoRepository;
        this.transactionService = transactionService;
    }

    public double getVendas(LocalDate start, LocalDate end) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));
        return vendas.stream().mapToDouble(Venda::getValorTotal).sum();
    }

    public long getNovosClientes(LocalDate start, LocalDate end) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                clienteRepository.countNewClientesBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));
    }

    public double getTicketMedio(LocalDate start, LocalDate end) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));
        if (vendas.isEmpty()) return 0.0;
        return vendas.stream().mapToDouble(Venda::getValorTotal).sum() / vendas.size();
    }

    public double getValorTotalInventario() throws PersistenciaException {
        List<Produto> produtos = transactionService.executeInTransactionWithResult(produtoRepository::findAll);
        return produtos.stream().mapToDouble(p -> p.getPreco() * p.getQuantidadeTotal()).sum();
    }

    public Map<String, Integer> getTopProdutos(LocalDate start, LocalDate end, int limit) throws PersistenciaException {
        List<VendaItem> itens = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendaItensBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));

        return itens.stream()
                .collect(Collectors.groupingBy(item -> item.getProduto().getNome(), Collectors.summingInt(VendaItem::getQuantidade)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, java.util.LinkedHashMap::new));
    }

    public Map<String, Double> getTopClientes(LocalDate start, LocalDate end, int limit) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));

        return vendas.stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(v -> v.getCliente().getNome(), Collectors.summingDouble(Venda::getValorTotal)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, java.util.LinkedHashMap::new));
    }

    public List<Object[]> getRecentActivity(int limit) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                auditoriaRepository.findRecentActivity(limit, em));
    }

    public Map<LocalDate, Double> getVendasUltimos7Dias() throws PersistenciaException {
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start, end, em));

        return vendas.stream()
                .collect(Collectors.groupingBy(
                        venda -> venda.getDataVenda().toLocalDate(),
                        Collectors.summingDouble(Venda::getValorTotal)
                ));
    }

    public String getTopSellingProduct() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            List<VendaItem> recentItems = vendaRepository.findAllItems(em);

            if (recentItems.isEmpty()) {
                return "Nenhuma venda registrada ainda.";
            }

            return recentItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getProduto().getNome(),
                            Collectors.summingInt(VendaItem::getQuantidade)
                    ))
                    .entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse("Não foi possível determinar o produto mais vendido.");
        });
    }

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