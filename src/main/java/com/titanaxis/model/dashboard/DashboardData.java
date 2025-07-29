package com.titanaxis.model.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardData {
    public final double vendasHoje;
    public final double receitaMes;
    public final int novosClientes;
    public final int numAlertas;
    public final Map<?, Double> salesChartData;
    public final double receitaMesAnterior;
    public final double ticketMedio;
    public final double ticketMedioMesAnterior;
    public final Map<String, Integer> topProducts;
    public final Map<String, Double> topClients;
    public final List<Insight> assistantInsights;
    public final Map<String, Map<LocalDate, Double>> categorySalesEvolution;
    public final List<CategoryTrend> categoryTrends; 

    public DashboardData(double vendasHoje, double receitaMes, int novosClientes, int numAlertas,
                         Map<?, Double> salesChartData, double receitaMesAnterior, double ticketMedio,
                         double ticketMedioMesAnterior, Map<String, Integer> topProducts,
                         Map<String, Double> topClients, List<Insight> assistantInsights,
                         Map<String, Map<LocalDate, Double>> categorySalesEvolution,
                         List<CategoryTrend> categoryTrends) { 
        this.vendasHoje = vendasHoje;
        this.receitaMes = receitaMes;
        this.novosClientes = novosClientes;
        this.numAlertas = numAlertas;
        this.salesChartData = salesChartData;
        this.receitaMesAnterior = receitaMesAnterior;
        this.ticketMedio = ticketMedio;
        this.ticketMedioMesAnterior = ticketMedioMesAnterior;
        this.topProducts = topProducts;
        this.topClients = topClients;
        this.assistantInsights = assistantInsights;
        this.categorySalesEvolution = categorySalesEvolution;
        this.categoryTrends = categoryTrends; 
    }
}