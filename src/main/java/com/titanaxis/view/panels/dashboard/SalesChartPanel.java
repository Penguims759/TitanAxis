// penguims759/titanaxis/Penguims759-TitanAxis-3548b4fb921518903cda130d6ede827719ea5192/src/main/java/com/titanaxis/view/panels/dashboard/SalesChartPanel.java
package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class SalesChartPanel extends JPanel {

    private Map<?, Double> salesData = Collections.emptyMap();
    private final Consumer<String> onPeriodChange;
    private final JLabel titleLabel;

    private static final int PADDING = 20;
    private static final int LEFT_PADDING = 50; // Espaço para rótulos do eixo Y
    private static final int BOTTOM_PADDING = 30; // Espaço para rótulos do eixo X
    private static final Color CHART_COLOR = new Color(70, 130, 180);
    private static final NumberFormat COMPACT_CURRENCY_FORMAT = NumberFormat.getCompactNumberInstance(new Locale("pt", "BR"), NumberFormat.Style.SHORT);

    public SalesChartPanel(Consumer<String> onPeriodChange) {
        this.onPeriodChange = onPeriodChange;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        titleLabel = new JLabel("Últimos 7 Dias", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(createPeriodSelectionPanel(), BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private JPanel createPeriodSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);
        ButtonGroup buttonGroup = new ButtonGroup();

        JToggleButton weekButton = createPeriodButton("7D", "7D", buttonGroup, true);
        JToggleButton monthButton = createPeriodButton("1M", "1M", buttonGroup, false);
        JToggleButton threeMonthsButton = createPeriodButton("3M", "3M", buttonGroup, false);
        JToggleButton yearButton = createPeriodButton("1A", "1A", buttonGroup, false);

        panel.add(weekButton);
        panel.add(monthButton);
        panel.add(threeMonthsButton);
        panel.add(yearButton);

        return panel;
    }

    private JToggleButton createPeriodButton(String text, String period, ButtonGroup group, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setFocusable(false);
        button.addActionListener(e -> onPeriodChange.accept(period));
        group.add(button);
        button.setSelected(selected);
        return button;
    }

    public void setData(Map<?, Double> salesData, String period) {
        this.salesData = salesData;
        updateTitle(period);
        // Garante que o painel é redesenhado com os novos dados
        revalidate();
        repaint();
    }

    private void updateTitle(String period) {
        switch (period) {
            case "1M":
                titleLabel.setText("Vendas do Mês Atual");
                break;
            case "3M":
                titleLabel.setText("Vendas nos Últimos 3 Meses");
                break;
            case "1A":
                titleLabel.setText("Vendas no Ano Atual");
                break;
            default:
                titleLabel.setText("Vendas nos Últimos 7 Dias");
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (salesData == null || salesData.isEmpty()) {
            String msg = "Sem dados de vendas para o período.";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(msg, x, y);
            return;
        }

        double maxValue = salesData.values().stream().max(Double::compare).orElse(1.0);
        if (maxValue == 0) maxValue = 1.0;

        drawYAxis(g2, maxValue);
        drawXAxisAndBars(g2, maxValue);
    }

    private void drawYAxis(Graphics2D g2, double maxValue) {
        int numberYDivisions = 5;
        // CORREÇÃO: A área útil de desenho é calculada a partir das dimensões do painel e dos espaçamentos
        int chartHeight = getHeight() - PADDING - BOTTOM_PADDING;

        for (int i = 0; i <= numberYDivisions; i++) {
            int y = getHeight() - BOTTOM_PADDING - (i * chartHeight) / numberYDivisions;

            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f}, 0.0f));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(LEFT_PADDING, y, getWidth() - PADDING, y);

            g2.setStroke(new BasicStroke());
            g2.setColor(UIManager.getColor("Label.foreground"));
            String yLabel = COMPACT_CURRENCY_FORMAT.format((maxValue * i) / numberYDivisions);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(yLabel);
            g2.drawString(yLabel, LEFT_PADDING - labelWidth - 8, y + 4);
        }
    }

    private void drawXAxisAndBars(Graphics2D g2, double maxValue) {
        List<?> keys = new ArrayList<>(salesData.keySet());
        int barCount = keys.size();
        if (barCount == 0) return;

        int chartWidth = getWidth() - PADDING - LEFT_PADDING;
        int chartHeight = getHeight() - PADDING - BOTTOM_PADDING;
        int barWidth = chartWidth / barCount;
        int barGap = (int) (barWidth * 0.2);

        for (int i = 0; i < barCount; i++) {
            Object key = keys.get(i);
            double value = salesData.getOrDefault(key, 0.0);
            int barHeightValue = (int) ((value / maxValue) * chartHeight);
            int x = LEFT_PADDING + i * barWidth;
            int y = getHeight() - BOTTOM_PADDING - barHeightValue;

            g2.setColor(CHART_COLOR);
            g2.fillRect(x + barGap, y, barWidth - (2 * barGap), barHeightValue);

            g2.setColor(UIManager.getColor("Label.foreground"));
            String xLabel = formatXAxisLabel(key);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(xLabel);
            g2.drawString(xLabel, x + (barWidth - labelWidth) / 2, getHeight() - BOTTOM_PADDING + fm.getAscent() + 5);
        }
    }

    private String formatXAxisLabel(Object key) {
        if (key instanceof LocalDate) {
            return ((LocalDate) key).format(DateTimeFormatter.ofPattern("dd/MM"));
        }
        if (key instanceof YearMonth) {
            return ((YearMonth) key).getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")).toUpperCase();
        }
        return key.toString();
    }
}