package com.titanaxis.view.panels.dashboard;

import com.titanaxis.util.I18n;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LineChartPanel extends JPanel {

    private Map<String, Map<LocalDate, Double>> data = Collections.emptyMap();
    private final List<Color> lineColors = List.of(
            new Color(75, 192, 192),
            new Color(255, 99, 132),
            new Color(54, 162, 235),
            new Color(255, 206, 86),
            new Color(153, 102, 255)
    );

    private static final int PADDING = 20;
    private static final int LEFT_PADDING = 60;
    private static final int BOTTOM_PADDING = 40;
    private static final NumberFormat COMPACT_CURRENCY_FORMAT = NumberFormat.getCompactNumberInstance(new Locale("pt", "BR"), NumberFormat.Style.SHORT);

    public LineChartPanel() {
        setOpaque(false);
    }

    public void setData(Map<String, Map<LocalDate, Double>> data) {
        this.data = (data != null) ? data : Collections.emptyMap();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data.isEmpty()) {
            String msg = I18n.getString("home.chart.noData");
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(msg, x, y);
            return;
        }

        drawAxes(g2);
        drawLines(g2);
    }

    private double getMaxValue() {
        return data.values().stream()
                .flatMap(map -> map.values().stream())
                .max(Double::compare)
                .orElse(1.0);
    }

    private List<LocalDate> getSortedDates() {
        return data.values().stream()
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private void drawAxes(Graphics2D g2) {
        double maxValue = getMaxValue();
        int width = getWidth();
        int height = getHeight();
        int chartHeight = height - PADDING - BOTTOM_PADDING;
        int numberYDivisions = 5;

        g2.setColor(UIManager.getColor("Label.foreground"));
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f}, 0.0f));
        g2.setColor(Color.LIGHT_GRAY);

        // Y-Axis lines and labels
        for (int i = 0; i <= numberYDivisions; i++) {
            int y = height - BOTTOM_PADDING - (i * chartHeight) / numberYDivisions;
            g2.drawLine(LEFT_PADDING, y, width - PADDING, y);
            String yLabel = COMPACT_CURRENCY_FORMAT.format((maxValue * i) / numberYDivisions);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(yLabel);
            g2.setColor(UIManager.getColor("Label.foreground"));
            g2.drawString(yLabel, LEFT_PADDING - labelWidth - 8, y + 4);
            g2.setColor(Color.LIGHT_GRAY);
        }

        // X-Axis labels
        List<LocalDate> dates = getSortedDates();
        int chartWidth = width - PADDING - LEFT_PADDING;
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(UIManager.getColor("Label.foreground"));
        for (int i = 0; i < dates.size(); i++) {
            if (i % (dates.size() / 5 + 1) == 0) { // Show only a few labels to avoid clutter
                int x = LEFT_PADDING + (i * chartWidth) / (dates.size() - 1);
                String xLabel = dates.get(i).format(DateTimeFormatter.ofPattern("dd/MM"));
                int labelWidth = fm.stringWidth(xLabel);
                g2.drawString(xLabel, x - labelWidth / 2, height - BOTTOM_PADDING + fm.getAscent() + 5);
            }
        }
    }

    private void drawLines(Graphics2D g2) {
        double maxValue = getMaxValue();
        List<LocalDate> dates = getSortedDates();
        if (dates.size() < 2) return;

        int width = getWidth();
        int height = getHeight();
        int chartWidth = width - PADDING - LEFT_PADDING;
        int chartHeight = height - PADDING - BOTTOM_PADDING;
        int colorIndex = 0;

        g2.setStroke(new BasicStroke(2.0f));

        for (Map.Entry<String, Map<LocalDate, Double>> series : data.entrySet()) {
            g2.setColor(lineColors.get(colorIndex % lineColors.size()));
            Point previousPoint = null;
            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = dates.get(i);
                double value = series.getValue().getOrDefault(date, 0.0);
                int x = LEFT_PADDING + (i * chartWidth) / (dates.size() - 1);
                int y = height - BOTTOM_PADDING - (int) ((value / maxValue) * chartHeight);
                Point currentPoint = new Point(x, y);

                if (previousPoint != null) {
                    g2.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
                }
                previousPoint = currentPoint;
            }
            colorIndex++;
        }
    }

    public JComponent getLegend() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setOpaque(false);
        int colorIndex = 0;
        for (String seriesName : data.keySet()) {
            JLabel label = new JLabel("● " + seriesName);
            label.setForeground(lineColors.get(colorIndex % lineColors.size()));
            legendPanel.add(label);
            colorIndex++;
        }
        return legendPanel;
    }
}