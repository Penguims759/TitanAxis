package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static final int LEFT_PADDING = 50;
    private static final int BOTTOM_PADDING = 30;
    private static final Color CHART_COLOR = new Color(70, 130, 180);
    private static final Color CHART_HOVER_COLOR = new Color(100, 160, 210);
    private static final NumberFormat COMPACT_CURRENCY_FORMAT = NumberFormat.getCompactNumberInstance(new Locale("pt", "BR"), NumberFormat.Style.SHORT);
    private int hoveredBarIndex = -1;

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

        // ADICIONADO: Listeners para interatividade
        addChartMouseListeners();
    }

    private void addChartMouseListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int barIndex = getBarIndexAt(e.getX());
                if (barIndex != -1) {
                    Object periodKey = new ArrayList<>(salesData.keySet()).get(barIndex);
                    showSalesDetailsForPeriod(periodKey);
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int barIndex = getBarIndexAt(e.getX());
                if (barIndex != hoveredBarIndex) {
                    hoveredBarIndex = barIndex;
                    repaint(); // Redesenha para mostrar o efeito hover
                }
            }
        });
    }

    private int getBarIndexAt(int mouseX) {
        if (salesData == null || salesData.isEmpty()) {
            return -1;
        }
        int barCount = salesData.size();
        int chartWidth = getWidth() - PADDING - LEFT_PADDING;
        int barWidth = chartWidth / barCount;

        int relativeX = mouseX - LEFT_PADDING;
        if (relativeX < 0) return -1;

        int index = relativeX / barWidth;
        return (index < barCount) ? index : -1;
    }

    private void showSalesDetailsForPeriod(Object periodKey) {
        // NOTA: Uma implementação completa aqui faria uma nova chamada de serviço
        // para buscar as vendas detalhadas do período e as exibiria num JDialog.
        // Para este exemplo, mostramos uma mensagem simples.
        String periodString = "";
        if (periodKey instanceof LocalDate) {
            periodString = ((LocalDate) periodKey).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (periodKey instanceof YearMonth) {
            periodString = ((YearMonth) periodKey).format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "BR")));
        }

        JOptionPane.showMessageDialog(this,
                "A mostrar detalhes de vendas para: " + periodString,
                "Detalhes de Vendas",
                JOptionPane.INFORMATION_MESSAGE);
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

            g2.setColor(i == hoveredBarIndex ? CHART_HOVER_COLOR : CHART_COLOR);
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