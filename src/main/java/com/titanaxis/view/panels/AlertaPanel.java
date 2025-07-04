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
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Alertas de Estoque", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        alertaTextArea = new JTextArea();
        alertaTextArea.setEditable(false);
        alertaTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        alertaTextArea.setLineWrap(true);
        alertaTextArea.setWrapStyleWord(true);
        add(new JScrollPane(alertaTextArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Atualizar Alertas");
        // ALTERADO: A ação agora é executada num SwingWorker para não bloquear a UI.
        refreshButton.addActionListener(e -> carregarAlertas());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        carregarAlertas();
    }

    private void carregarAlertas() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                logger.info("Carregando alertas...");
                return alertaService.gerarMensagensDeAlerta();
            }

            @Override
            protected void done() {
                try {
                    List<String> mensagens = get();
                    StringBuilder sb = new StringBuilder();
                    if (mensagens.isEmpty()) {
                        sb.append("Nenhum alerta de estoque ativo.");
                    } else {
                        for (String msg : mensagens) {
                            sb.append(msg).append("\n\n");
                        }
                    }
                    alertaTextArea.setText(sb.toString());
                    alertaTextArea.setCaretPosition(0);
                    logger.info("Alertas carregados e exibidos.");
                } catch (Exception e) {
                    logger.severe("Falha ao carregar alertas: " + e.getMessage());
                    alertaTextArea.setText("Ocorreu um erro ao carregar os alertas.\nConsulte os logs para mais detalhes.");
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }
}