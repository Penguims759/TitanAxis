// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/AlertaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.AlertaService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIMessageUtil; // Importado

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
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
        refreshButton.addActionListener(e -> refreshData()); // ALTERADO: Chama refreshData()
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshData(); // Chama o refresh inicial
    }

    // NOVO MÉTODO: Para ser chamado externamente (e.g., pelo DashboardFrame) para recarregar os dados
    public void refreshData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
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
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    logger.log(Level.SEVERE, "Falha ao carregar alertas.", cause);
                    String errorMessage = "Ocorreu um erro ao carregar os alertas.";
                    if (cause instanceof PersistenciaException) {
                        errorMessage = "Erro de Base de Dados: " + cause.getMessage();
                    }
                    UIMessageUtil.showErrorMessage(AlertaPanel.this, errorMessage + "\nConsulte os logs para mais detalhes.", "Erro");
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }
}