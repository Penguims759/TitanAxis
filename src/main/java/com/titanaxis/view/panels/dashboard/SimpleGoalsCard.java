package com.titanaxis.view.panels.dashboard;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MetaVenda;
import com.titanaxis.service.AnalyticsService;
import com.titanaxis.service.FinanceiroService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.DashboardFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimpleGoalsCard extends JPanel implements DashboardFrame.Refreshable {

    private final FinanceiroService financeiroService;
    private final AnalyticsService analyticsService;
    private final JPanel goalsContainer;

    public SimpleGoalsCard(AppContext appContext) {
        this.financeiroService = appContext.getFinanceiroService();
        this.analyticsService = appContext.getAnalyticsService();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(I18n.getString("goals.panel.title")));

        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(goalsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        goalsContainer.removeAll();
        try {
            List<MetaVenda> metas = financeiroService.listarMetas();
            boolean hasGoals = false;

            // Estrutura para armazenar a meta com seu progresso calculado
            record MetaComProgresso(MetaVenda meta, double progresso) {}
            List<MetaComProgresso> metasProcessadas = new ArrayList<>();

            for (MetaVenda meta : metas) {
                YearMonth periodo = YearMonth.parse(meta.getAnoMes());
                if(periodo.isBefore(YearMonth.now())) continue;

                hasGoals = true;
                LocalDate inicioPeriodo = periodo.atDay(1);
                LocalDate fimPeriodo = periodo.atEndOfMonth();

                double valorVendido = analyticsService.getVendasPorVendedorNoPeriodo(meta.getUsuario().getId(), inicioPeriodo, fimPeriodo);
                double progresso = (meta.getValorMeta() > 0) ? (valorVendido / meta.getValorMeta()) * 100 : 0;

                metasProcessadas.add(new MetaComProgresso(meta, progresso));
            }

            // ALTERAÇÃO: Ordena a lista pelo progresso em ordem decrescente
            metasProcessadas.sort(Comparator.comparingDouble(MetaComProgresso::progresso).reversed());

            if (!hasGoals) {
                goalsContainer.add(new JLabel("Nenhuma meta ativa para o período."));
            } else {
                // Popula a UI com os dados já ordenados
                for(MetaComProgresso metaProcessada : metasProcessadas) {
                    goalsContainer.add(createGoalEntry(metaProcessada.meta().getUsuario().getNomeUsuario(), (int) metaProcessada.progresso()));
                    goalsContainer.add(Box.createVerticalStrut(5));
                }
            }

        } catch (PersistenciaException e) {
            goalsContainer.add(new JLabel("Erro ao carregar metas."));
        }
        goalsContainer.revalidate();
        goalsContainer.repaint();
    }


    private JComponent createGoalEntry(String userName, int progress) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(nameLabel, gbc);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%d%%", progress));
        progressBar.setForeground(new Color(34, 139, 34));
        progressBar.setPreferredSize(new Dimension(200, 25));

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(progressBar, gbc);

        return panel;
    }
}