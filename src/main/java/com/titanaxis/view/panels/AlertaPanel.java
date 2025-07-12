// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/AlertaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.service.AlertaService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

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

        JLabel titleLabel = new JLabel(I18n.getString("alert.panel.title"), SwingConstants.CENTER); // ALTERADO
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        alertaTextArea = new JTextArea();
        alertaTextArea.setEditable(false);
        alertaTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        alertaTextArea.setLineWrap(true);
        alertaTextArea.setWrapStyleWord(true);
        alertaTextArea.setFocusable(false);
        add(new JScrollPane(alertaTextArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton(I18n.getString("alert.panel.refreshButton")); // ALTERADO
        refreshButton.addActionListener(e -> refreshData());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshData();
    }

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
                    if (mensagens.isEmpty() || (mensagens.size() == 1 && mensagens.get(0).startsWith("Nenhum"))) { // Lógica para tratar o caso de nenhum alerta
                        sb.append(I18n.getString("alert.panel.noAlerts")); // ALTERADO
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
                    String errorMessage = I18n.getString("alert.panel.error.load"); // ALTERADO
                    if (cause instanceof PersistenciaException) {
                        errorMessage = I18n.getString("error.db.generic", cause.getMessage()); // ALTERADO
                    }
                    UIMessageUtil.showErrorMessage(AlertaPanel.this, errorMessage + "\n" + I18n.getString("error.seeLogs"), I18n.getString("error.title")); // ALTERADO
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }
}