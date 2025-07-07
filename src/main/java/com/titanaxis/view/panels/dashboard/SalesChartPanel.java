package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesChartPanel extends JPanel {

    private Map<LocalDate, Double> salesData = Collections.emptyMap();
    private static final int PADDING = 25;
    private static final int LABEL_PADDING = 40;
    private static final Color CHART_COLOR = new Color(70, 130, 180);

    public SalesChartPanel() {
        setBorder(BorderFactory.createTitledBorder("Vendas dos Últimos 7 Dias"));
    }

    public void setData(Map<LocalDate, Double> salesData) {
        this.salesData = salesData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (salesData == null || salesData.isEmpty()) {
            String msg = "Sem dados de vendas nos últimos 7 dias.";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(msg, x, y);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double maxValue = salesData.values().stream().max(Double::compare).orElse(1.0);
        if (maxValue == 0) maxValue = 1.0; // Evita divisão por zero
        int width = getWidth();
        int height = getHeight();

        int numberYDivisions = 6;
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = PADDING + LABEL_PADDING;
            int x1 = width - PADDING;
            int y0 = height - ((i * (height - PADDING * 2 - LABEL_PADDING)) / numberYDivisions + PADDING + LABEL_PADDING);
            if (i > 0) {
                g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f}, 0.0f));
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(x0, y0, x1, y0);
            }
            g2.setStroke(new BasicStroke());
            g2.setColor(Color.DARK_GRAY);
            String yLabel = String.format("€ %.0f", (maxValue * i) / numberYDivisions);
            g2.drawString(yLabel, PADDING, y0 + 4);
        }

        int barCount = 7; // Sempre mostrar 7 dias
        int barWidth = (width - 2 * PADDING - LABEL_PADDING) / barCount;
        int barGap = 10;

        for (int i = 0; i < barCount; i++) {
            LocalDate date = LocalDate.now().minusDays((barCount - 1) - i);
            double value = salesData.getOrDefault(date, 0.0);

            int barHeight = (int) ((value / maxValue) * (height - 2 * PADDING - LABEL_PADDING));
            int x = PADDING + LABEL_PADDING + i * barWidth;
            int y = height - PADDING - LABEL_PADDING - barHeight;
            g2.setColor(CHART_COLOR);
            g2.fillRect(x + barGap, y, barWidth - 2 * barGap, barHeight);

            g2.setColor(Color.DARK_GRAY);
            String xLabel = date.format(DateTimeFormatter.ofPattern("dd/MM"));
            g2.drawString(xLabel, x + (barWidth / 2) - 15, height - PADDING + 5);
        }
    }
}