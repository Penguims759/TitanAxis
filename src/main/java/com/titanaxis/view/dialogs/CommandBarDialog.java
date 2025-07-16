package com.titanaxis.view.dialogs;

import com.titanaxis.app.AppContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CommandBarDialog extends JDialog {

    private final AppContext appContext;
    private JTextField commandField;
    private JList<String> resultsList;
    private DefaultListModel<String> listModel;

    public CommandBarDialog(Frame owner, AppContext appContext) {
        super(owner, true);
        this.appContext = appContext;
        initComponents();
    }

    private void initComponents() {
        setUndecorated(true);
        setSize(600, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIManager.getColor("Panel.background"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UIManager.getColor("Separator.foreground"));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        commandField = new JTextField();
        commandField.setFont(new Font("Arial", Font.PLAIN, 18));
        commandField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setFont(new Font("Arial", Font.PLAIN, 14));

        contentPanel.add(commandField, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(resultsList), BorderLayout.CENTER);

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        add(contentPanel);
    }
}