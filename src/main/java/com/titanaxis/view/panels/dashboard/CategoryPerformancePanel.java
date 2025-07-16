package com.titanaxis.view.panels.dashboard;

import com.titanaxis.model.CategoryTrend;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CategoryPerformancePanel extends JPanel {

    private final LineChartPanel lineChart;
    private final RankingListPanel rankingList;
    private JComponent chartLegend;

    public CategoryPerformancePanel() {
        // **INÍCIO DA CORREÇÃO DEFINITIVA: Substituição do JSplitPane**
        // Usar BorderLayout para controlo total e sem divisores.
        setLayout(new BorderLayout(10, 0)); // Um espaçamento horizontal de 10px
        setBorder(BorderFactory.createTitledBorder("Evolução e Performance das Categorias (30 dias)"));

        lineChart = new LineChartPanel();
        rankingList = new RankingListPanel();
        chartLegend = lineChart.getLegend();

        // Painel para o gráfico e sua legenda
        JPanel chartContainer = new JPanel(new BorderLayout(0, 5));
        chartContainer.setOpaque(false);
        chartContainer.add(lineChart, BorderLayout.CENTER);
        chartContainer.add(chartLegend, BorderLayout.SOUTH);

        // Painel para a lista de ranking com largura fixa
        JScrollPane rankingScrollPane = new JScrollPane(rankingList);
        rankingScrollPane.setBorder(null);
        rankingScrollPane.setPreferredSize(new Dimension(350, 0)); // Largura fixa

        // Adiciona os componentes ao painel principal
        add(chartContainer, BorderLayout.CENTER); // Gráfico ocupa o espaço central
        add(rankingScrollPane, BorderLayout.EAST);   // Ranking fica fixo à direita
        // **FIM DA CORREÇÃO DEFINITIVA**
    }

    public void setData(Map<String, Map<LocalDate, Double>> evolutionData, List<CategoryTrend> trendData) {
        lineChart.setData(evolutionData);
        rankingList.setRankingData(trendData);

        // Atualiza a legenda dinamicamente
        JPanel chartContainer = (JPanel) lineChart.getParent();
        if (chartLegend != null) {
            chartContainer.remove(chartLegend);
        }
        chartLegend = lineChart.getLegend();
        chartContainer.add(chartLegend, BorderLayout.SOUTH);
        chartContainer.revalidate();
        chartContainer.repaint();
    }
}