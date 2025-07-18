package com.titanaxis.view.panels.dashboard;

import com.titanaxis.model.CategoryTrend;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingListPanel extends JPanel {

    private final JPanel container;

    public RankingListPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        // ALTERAÇÃO: Adiciona uma margem interna de 10 pixels em todos os lados.
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(container, BorderLayout.NORTH);
    }

    public void setRankingData(List<CategoryTrend> trends) {
        container.removeAll();
        if (trends == null || trends.isEmpty()) {
            container.add(new JLabel("Sem dados de tendência."));
            revalidateAndRepaint();
            return;
        }

        int rank = 1;
        for (CategoryTrend trend : trends) {
            container.add(createRankingEntry(rank++, trend.getCategoryName(), trend.getPercentageChange()));
            container.add(Box.createVerticalStrut(10));
        }

        revalidateAndRepaint();
    }

    private Component createRankingEntry(int rank, String name, double percentage) {
        JPanel entryPanel = new JPanel(new BorderLayout(10, 0));
        entryPanel.setOpaque(false);

        JLabel rankLabel = new JLabel(String.format("%d.", rank));
        rankLabel.setFont(new Font("Arial", Font.BOLD, 14));
        entryPanel.add(rankLabel, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        entryPanel.add(nameLabel, BorderLayout.CENTER);

        JLabel trendLabel = new JLabel();
        trendLabel.setFont(new Font("Arial", Font.BOLD, 14));
        if (percentage > 0) {
            trendLabel.setText(String.format("▲ %.1f%%", percentage));
            trendLabel.setForeground(new Color(34, 139, 34));
        } else {
            trendLabel.setText(String.format("▼ %.1f%%", Math.abs(percentage)));
            trendLabel.setForeground(Color.RED);
        }
        entryPanel.add(trendLabel, BorderLayout.EAST);

        return entryPanel;
    }

    private void revalidateAndRepaint() {
        container.revalidate();
        container.repaint();
    }
}