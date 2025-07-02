package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.service.AlertaService;
import com.titanaxis.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

public class AlertaPanel extends JPanel {
    private final AlertaService alertaService;
    private final JTextArea alertaTextArea;
    private static final Logger logger = AppLogger.getLogger();

    public AlertaPanel(AppContext appContext) {
        this.alertaService = appContext.getAlertaService();
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Alertas de Estoque", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        alertaTextArea = new JTextArea();
        alertaTextArea.setEditable(false);
        alertaTextArea.setLineWrap(true);
        alertaTextArea.setWrapStyleWord(true);
        add(new JScrollPane(alertaTextArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Atualizar Alertas");
        refreshButton.addActionListener(e -> loadAlerts());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAlerts();
    }

    private void loadAlerts() {
        logger.info("Carregando alertas...");
        List<String> mensagens = alertaService.gerarMensagensDeAlerta();
        StringBuilder sb = new StringBuilder();
        for (String msg : mensagens) {
            sb.append(msg).append("\n\n");
        }
        alertaTextArea.setText(sb.toString());
        alertaTextArea.setCaretPosition(0); // Rola para o topo
        logger.info("Alertas carregados e exibidos.");
    }
}