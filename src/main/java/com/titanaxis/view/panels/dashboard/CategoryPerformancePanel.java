package com.titanaxis.view.panels.dashboard;

import com.titanaxis.model.dashboard.CategoryTrend;
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
        // CORREÇÃO: Usa GridBagLayout para controle total do espaço interno.
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Evolução e Performance das Categorias (30 dias)"));
        GridBagConstraints gbc = new GridBagConstraints();

        // --- PAINEL DO GRÁFICO (ESQUERDA) ---
        lineChart = new LineChartPanel();
        chartLegend = lineChart.getLegend();
        JPanel chartContainer = new JPanel(new BorderLayout(0, 5));
        chartContainer.setOpaque(false);
        chartContainer.add(lineChart, BorderLayout.CENTER);
        chartContainer.add(chartLegend, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Ocupa a maior parte da largura.
        gbc.weighty = 1.0; // Ocupa toda a altura disponível.
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10);
        add(chartContainer, gbc);

        // --- PAINEL DO RANKING (DIREITA) ---
        rankingList = new RankingListPanel();
        JScrollPane rankingScrollPane = new JScrollPane(rankingList);
        rankingScrollPane.setBorder(null);

        gbc.gridx = 1;
        gbc.weightx = 0; // Não "estica" na horizontal.
        gbc.fill = GridBagConstraints.VERTICAL; // Preenche apenas na vertical.
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Define uma largura fixa para o painel de ranking.
        JPanel rankingContainer = new JPanel(new BorderLayout());
        rankingContainer.setPreferredSize(new Dimension(300, 100)); // A altura de 1 é ignorada.
        rankingContainer.add(rankingScrollPane, BorderLayout.CENTER);
        add(rankingContainer, gbc);
    }

    public void setData(Map<String, Map<LocalDate, Double>> evolutionData, List<CategoryTrend> trendData) {
        lineChart.setData(evolutionData);
        rankingList.setRankingData(trendData);

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