package com.titanaxis.view.panels.dashboard;

import com.titanaxis.util.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PieChartPanel extends JPanel {

    private Map<String, Double> data = Collections.emptyMap();
    private final String title;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final List<Color> sliceColors = List.of(
            new Color(255, 99, 132),
            new Color(54, 162, 235),
            new Color(255, 206, 86),
            new Color(75, 192, 192),
            new Color(153, 102, 255)
    );

    public PieChartPanel(String title) {
        this.title = title;
        setOpaque(false);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height) - 40;
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2;

        if (data == null || data.isEmpty()) {
            g2.setColor(UIManager.getColor("Label.foreground"));
            String noDataMsg = I18n.getString("home.chart.noData");
            FontMetrics fm = g2.getFontMetrics();
            int msgWidth = fm.stringWidth(noDataMsg);
            g2.drawString(noDataMsg, (width - msgWidth) / 2, height / 2);
            return;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        double startAngle = 0;
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double extent = (entry.getValue() / total) * 360;
            g2.setColor(sliceColors.get(colorIndex % sliceColors.size()));
            g2.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, extent, Arc2D.PIE));
            startAngle += extent;
            colorIndex++;
        }
    }

    public JComponent getDescriptionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (data == null || data.isEmpty()) {
            panel.add(new JLabel(I18n.getString("home.chart.noData")));
            return panel;
        }

        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
            JLabel colorSquare = new JLabel("■");
            colorSquare.setForeground(sliceColors.get(colorIndex % sliceColors.size()));
            colorSquare.setFont(new Font("Arial", Font.BOLD, 20));
            line.add(colorSquare);
            line.add(new JLabel(String.format("%s: %s", entry.getKey(), currencyFormat.format(entry.getValue()))));
            panel.add(line);
            colorIndex++;
        }

        return new JScrollPane(panel);
    }
}