// penguims759/titanaxis/Penguims759-TitanAxis-3548b4fb921518903cda130d6ede827719ea5192/src/main/java/com/titanaxis/service/AnalyticsService.java
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
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

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

    public Map<?, Double> getVendasAgrupadas(String period) throws PersistenciaException {
        LocalDate today = LocalDate.now();
        LocalDate start;
        ChronoUnit groupByUnit;

        switch (period) {
            case "1M":
                start = today.withDayOfMonth(1);
                groupByUnit = ChronoUnit.DAYS;
                break;
            case "3M":
                start = today.minusMonths(3).withDayOfMonth(1);
                groupByUnit = ChronoUnit.MONTHS;
                break;
            case "1A":
                start = today.withDayOfYear(1);
                groupByUnit = ChronoUnit.MONTHS;
                break;
            default: // "7D"
                start = today.minusDays(6);
                groupByUnit = ChronoUnit.DAYS;
                break;
        }

        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), today.atTime(LocalTime.MAX), em));

        if (groupByUnit == ChronoUnit.DAYS) {
            Map<LocalDate, Double> dailyData = Stream.iterate(start, date -> date.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(start, today) + 1)
                    .collect(Collectors.toMap(date -> date, date -> 0.0, (v1, v2) -> v1, LinkedHashMap::new));

            vendas.forEach(venda -> dailyData.merge(venda.getDataVenda().toLocalDate(), venda.getValorTotal(), Double::sum));
            return dailyData;
        } else { // Agrupar por mês
            Map<YearMonth, Double> monthlyData = Stream.iterate(YearMonth.from(start), ym -> ym.plusMonths(1))
                    .limit(ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(today)) + 1)
                    .collect(Collectors.toMap(ym -> ym, ym -> 0.0, (v1, v2) -> v1, LinkedHashMap::new));

            vendas.forEach(venda -> monthlyData.merge(YearMonth.from(venda.getDataVenda()), venda.getValorTotal(), Double::sum));
            return monthlyData;
        }
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