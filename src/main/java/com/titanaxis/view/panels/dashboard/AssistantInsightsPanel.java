package com.titanaxis.view.panels.dashboard;

import com.titanaxis.model.dashboard.Insight;
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

        // O container principal agora usa GridBagLayout para controlo total.
        container = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setInsights(List<Insight> insights) {
        container.removeAll();

        if (insights == null || insights.isEmpty()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            container.add(new JLabel("  Nenhum insight ou alerta no momento."), gbc);
            revalidateAndRepaint();
            return;
        }

        Map<Insight.InsightType, List<Insight>> groupedInsights = insights.stream()
                .collect(Collectors.groupingBy(Insight::getType));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (Map.Entry<Insight.InsightType, List<Insight>> entry : groupedInsights.entrySet()) {
            // Adiciona o cabeçalho do grupo
            gbc.insets = new Insets(gbc.gridy == 0 ? 5 : 15, 5, 5, 5); // Espaço maior entre grupos
            container.add(createGroupHeader(entry.getKey().getTypeName()), gbc);
            gbc.gridy++;

            // Adiciona os insights do grupo
            for (Insight insight : entry.getValue()) {
                gbc.insets = new Insets(2, 5, 2, 5); // Espaço menor entre itens do mesmo grupo
                container.add(createInsightComponent(insight), gbc);
                gbc.gridy++;
            }
        }

        // Adiciona um componente "espaçador" para empurrar tudo para cima
        gbc.weighty = 1.0;
        container.add(Box.createVerticalGlue(), gbc);

        revalidateAndRepaint();
    }

    private void revalidateAndRepaint() {
        container.revalidate();
        container.repaint();
    }

    private Component createGroupHeader(String title) {
        // Título centralizado e em negrito.
        JLabel header = new JLabel(title);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        return header;
    }

    private Component createInsightComponent(Insight insight) {
        // Painel para o item de insight, usando BorderLayout.
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ícone à esquerda.
        if (insight.getIcon() != null) {
            JLabel iconLabel = new JLabel(insight.getIcon());
            iconLabel.setVerticalAlignment(SwingConstants.NORTH);
            panel.add(iconLabel, BorderLayout.WEST);
        }

        // JTextArea para o texto.
        JTextArea textArea = new JTextArea(insight.getText());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setForeground(insight.getColor());
        textArea.setBorder(null);

        panel.add(textArea, BorderLayout.CENTER);

        // Listener de clique.
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (insight.getAction() != null) {
                    insight.getAction().run();
                }
            }
        };

        panel.addMouseListener(clickListener);
        textArea.addMouseListener(clickListener);

        return panel;
    }
}