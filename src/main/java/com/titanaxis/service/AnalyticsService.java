package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.*;
import com.titanaxis.model.dashboard.CategoryTrend;
import com.titanaxis.model.dashboard.Insight;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Color;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final AlertaService alertaService;
    private final TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Inject
    public AnalyticsService(VendaRepository vendaRepository, ClienteRepository clienteRepository, AuditoriaRepository auditoriaRepository, ProdutoRepository produtoRepository, AlertaService alertaService, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.clienteRepository = clienteRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.produtoRepository = produtoRepository;
        this.alertaService = alertaService;
        this.transactionService = transactionService;
    }

    public double getVendas(LocalDate start, LocalDate end) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));
        return vendas.stream().mapToDouble(Venda::getValorTotal).sum();
    }

    public double getVendasPorVendedorNoPeriodo(int usuarioId, LocalDate start, LocalDate end) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX), em));

        return vendas.stream()
                .filter(v -> v.getUsuario() != null && v.getUsuario().getId() == usuarioId)
                .mapToDouble(Venda::getValorTotal)
                .sum();
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
        } else {
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
                return I18n.getString("service.analytics.noSalesYet");
            }

            return recentItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getProduto().getNome(),
                            Collectors.summingInt(VendaItem::getQuantidade)
                    ))
                    .entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(I18n.getString("service.analytics.topProductError"));
        });
    }

    public String getProactiveInsightsSummary() {
        try {
            String topProduct = getTopSellingProduct();
            if (topProduct.contains(I18n.getString("service.analytics.noSalesYet"))) {
                return "";
            }
            return I18n.getString("service.analytics.proactiveInsight", topProduct);
        } catch (PersistenciaException e) {
            return I18n.getString("service.analytics.proactiveInsightError");
        }
    }

    public List<CategoryTrend> getTopCategoriesWithTrend(int limit, int days) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime mid = end.minusDays(days / 2);
            LocalDateTime start = end.minusDays(days);

            String topCategoriesQueryStr =
                    "SELECT p.categoria_id FROM venda_itens vi " +
                            "JOIN produtos p ON vi.produto_id = p.id " +
                            "JOIN vendas v ON vi.venda_id = v.id " +
                            "WHERE v.data_venda BETWEEN :start AND :end AND p.categoria_id IS NOT NULL " +
                            "GROUP BY p.categoria_id " +
                            "ORDER BY SUM(vi.preco_unitario * vi.quantidade) DESC";

            List<Integer> topCategoryIds = em.createNativeQuery(topCategoriesQueryStr, Integer.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setMaxResults(limit)
                    .getResultList();

            if (topCategoryIds.isEmpty()) {
                return Collections.emptyList();
            }

            String trendQueryStr =
                    "SELECT c.nome, " +
                            "SUM(CASE WHEN v.data_venda BETWEEN :start AND :mid THEN vi.preco_unitario * vi.quantidade ELSE 0 END) as previous_sales, " +
                            "SUM(CASE WHEN v.data_venda BETWEEN :mid AND :end THEN vi.preco_unitario * vi.quantidade ELSE 0 END) as current_sales " +
                            "FROM venda_itens vi " +
                            "JOIN produtos p ON vi.produto_id = p.id " +
                            "JOIN categorias c ON p.categoria_id = c.id " +
                            "JOIN vendas v ON vi.venda_id = v.id " +
                            "WHERE c.id IN (:categoryIds) AND v.data_venda BETWEEN :start AND :end " +
                            "GROUP BY c.nome";

            List<Object[]> results = em.createNativeQuery(trendQueryStr)
                    .setParameter("start", start)
                    .setParameter("mid", mid)
                    .setParameter("end", end)
                    .setParameter("categoryIds", topCategoryIds)
                    .getResultList();

            List<CategoryTrend> trends = new ArrayList<>();
            for (Object[] row : results) {
                String name = (String) row[0];
                double previousSales = ((Number) row[1]).doubleValue();
                double currentSales = ((Number) row[2]).doubleValue();

                double percentageChange = 0.0;
                if (previousSales > 0) {
                    percentageChange = ((currentSales - previousSales) / previousSales) * 100;
                } else if (currentSales > 0) {
                    percentageChange = 100.0;
                }
                trends.add(new CategoryTrend(name, percentageChange));
            }
            return trends;
        });
    }

    public Map<String, Map<LocalDate, Double>> getSalesEvolutionForTopCategories(int limit, int days) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusDays(days);

            String topCategoriesQueryStr =
                    "SELECT p.categoria_id FROM venda_itens vi " +
                            "JOIN produtos p ON vi.produto_id = p.id " +
                            "JOIN vendas v ON vi.venda_id = v.id " +
                            "WHERE v.data_venda BETWEEN :start AND :end AND p.categoria_id IS NOT NULL " +
                            "GROUP BY p.categoria_id " +
                            "ORDER BY SUM(vi.preco_unitario * vi.quantidade) DESC";

            List<Integer> topCategoryIds = em.createNativeQuery(topCategoriesQueryStr, Integer.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setMaxResults(limit)
                    .getResultList();

            if (topCategoryIds.isEmpty()) {
                return Collections.emptyMap();
            }

            String salesEvolutionQueryStr =
                    "SELECT c.nome, CAST(v.data_venda AS DATE) as dia, SUM(vi.preco_unitario * vi.quantidade) as total " +
                            "FROM venda_itens vi " +
                            "JOIN produtos p ON vi.produto_id = p.id " +
                            "JOIN categorias c ON p.categoria_id = c.id " +
                            "JOIN vendas v ON vi.venda_id = v.id " +
                            "WHERE v.data_venda BETWEEN :start AND :end AND c.id IN (:categoryIds) " +
                            "GROUP BY c.nome, dia " +
                            "ORDER BY dia";

            List<Object[]> results = em.createNativeQuery(salesEvolutionQueryStr)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setParameter("categoryIds", topCategoryIds)
                    .getResultList();

            Map<String, Map<LocalDate, Double>> evolutionData = new LinkedHashMap<>();
            for (Object[] row : results) {
                String categoryName = (String) row[0];
                LocalDate day = ((java.sql.Date) row[1]).toLocalDate();
                Double total = ((Number) row[2]).doubleValue();

                evolutionData.computeIfAbsent(categoryName, k -> new LinkedHashMap<>()).put(day, total);
            }

            LocalDate startDate = start.toLocalDate();
            LocalDate endDate = end.toLocalDate();
            for (Map.Entry<String, Map<LocalDate, Double>> entry : evolutionData.entrySet()) {
                Map<LocalDate, Double> dailySales = entry.getValue();
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    dailySales.putIfAbsent(date, 0.0);
                }
            }

            return evolutionData;
        });
    }

    public List<String> getSystemInsightsSummaryText() throws PersistenciaException {
        List<String> insightsText = new ArrayList<>();
        alertaService.getProdutosComEstoqueBaixo().stream()
                .findFirst()
                .ifPresent(p -> insightsText.add(I18n.getString("service.analytics.insight.lowStock", p.getNome(), p.getQuantidadeTotal())));

        alertaService.getLotesProximosDoVencimento().stream()
                .min(Comparator.comparing(Lote::getDataValidade))
                .ifPresent(l -> insightsText.add(I18n.getString("service.analytics.insight.expiringBatch", l.getNumeroLote(), l.getProduto().getNome(), l.getDataValidade().format(DATE_FORMATTER))));

        String topProduct = getTopSellingProduct();
        if (!topProduct.contains(I18n.getString("service.analytics.noSalesYet"))) {
            insightsText.add(I18n.getString("service.analytics.insight.topProduct", topProduct));
        }

        transactionService.executeInTransactionWithResult(vendaRepository::findAll).stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(Venda::getCliente, Collectors.maxBy(Comparator.comparing(Venda::getDataVenda))))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && ChronoUnit.DAYS.between(entry.getValue().get().getDataVenda(), LocalDateTime.now()) > 45)
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(c -> insightsText.add(I18n.getString("service.analytics.insight.inactiveClient", c.getNome())));

        return insightsText;
    }

    public List<Insight> getSystemInsightsSummary(DashboardFrame frame) throws PersistenciaException {
        List<Insight> insights = new ArrayList<>();
        Icon alertIcon = UIManager.getIcon("OptionPane.warningIcon");
        Icon opportunityIcon = UIManager.getIcon("OptionPane.informationIcon");

        alertaService.getProdutosComEstoqueBaixo().stream()
                .findFirst()
                .ifPresent(p -> {
                    String text = I18n.getString("service.analytics.insight.lowStock", p.getNome(), p.getQuantidadeTotal());
                    Runnable action = () -> frame.navigateTo("Produtos & Estoque");
                    insights.add(new Insight(text, Insight.InsightType.STOCK_ALERT, alertIcon, Color.ORANGE, action));
                });

        alertaService.getLotesProximosDoVencimento().stream()
                .min(Comparator.comparing(Lote::getDataValidade))
                .ifPresent(l -> {
                    String text = I18n.getString("service.analytics.insight.expiringBatch", l.getNumeroLote(), l.getProduto().getNome(), l.getDataValidade().format(DATE_FORMATTER));
                    Runnable action = () -> frame.navigateTo("Produtos & Estoque");
                    insights.add(new Insight(text, Insight.InsightType.STOCK_ALERT, alertIcon, Color.ORANGE, action));
                });

        String topProduct = getTopSellingProduct();
        if (!topProduct.contains(I18n.getString("service.analytics.noSalesYet"))) {
            insights.add(new Insight(I18n.getString("service.analytics.insight.topProduct", topProduct),
                    Insight.InsightType.OPPORTUNITY, opportunityIcon, UIManager.getColor("Label.foreground"), null));
        }

        transactionService.executeInTransactionWithResult(vendaRepository::findAll).stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(Venda::getCliente, Collectors.maxBy(Comparator.comparing(Venda::getDataVenda))))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && ChronoUnit.DAYS.between(entry.getValue().get().getDataVenda(), LocalDateTime.now()) > 45)
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(c -> {
                    String text = I18n.getString("service.analytics.insight.inactiveClient", c.getNome());
                    Runnable action = () -> frame.navigateTo("Clientes");
                    insights.add(new Insight(text, Insight.InsightType.OPPORTUNITY, opportunityIcon, new Color(70, 130, 180), action));
                });

        return insights;
    }

    public String getClientPurchaseHistory(int clienteId) throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findVendasByClienteId(clienteId, em)
        );

        if (vendas.isEmpty()) {
            return I18n.getString("service.analytics.history.noPurchases");
        }

        StringBuilder history = new StringBuilder(I18n.getString("service.analytics.history.header") + "\n");
        for (Venda venda : vendas) {
            history.append(I18n.getString("service.analytics.history.saleLine", venda.getId(), venda.getDataVenda().format(DATE_FORMATTER), CURRENCY_FORMAT.format(venda.getValorTotal()))).append("\n");
            for (VendaItem item : venda.getItens()) {
                history.append(I18n.getString("service.analytics.history.itemLine", item.getQuantidade(), item.getProduto().getNome())).append("\n");
            }
        }
        return history.toString();
    }
}