package com.titanaxis.view.panels.dashboard;

import com.titanaxis.model.Insight;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssistantInsightsPanel extends JPanel {

    private final JPanel container;

    public AssistantInsightsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Relatório e Ações do Assistente"));

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setInsights(List<Insight> insights) {
        container.removeAll();

        if (insights == null || insights.isEmpty()) {
            container.add(new JLabel("  Nenhum insight ou alerta no momento."));
            revalidateAndRepaint();
            return;
        }

        Map<Insight.InsightType, List<Insight>> groupedInsights = insights.stream()
                .collect(Collectors.groupingBy(Insight::getType));

        groupedInsights.forEach((type, insightList) -> {
            container.add(createGroupHeader(type.getTypeName()));
            insightList.forEach(insight -> container.add(createInsightLabel(insight)));
            container.add(Box.createVerticalStrut(10));
        });

        revalidateAndRepaint();
    }

    private void revalidateAndRepaint() {
        container.revalidate();
        container.repaint();
    }

    private JLabel createGroupHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return header;
    }

    private JLabel createInsightLabel(Insight insight) {
        JLabel label = new JLabel(insight.getText(), insight.getIcon(), SwingConstants.LEFT);
        label.setForeground(insight.getColor());
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 5));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (insight.getAction() != null) {
                    insight.getAction().run();
                }
            }
        });
        return label;
    }
}