package com.titanaxis.view.panels.dashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivityCardPanel extends JPanel {

    private final DefaultListModel<String> listModel;

    public ActivityCardPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Atividade Recente"));

        listModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(listModel);
        activityList.setCellRenderer(new ActivityCellRenderer());

        add(new JScrollPane(activityList), BorderLayout.CENTER);
    }

    public void setActivities(List<Object[]> activities) {
        listModel.clear();
        if (activities.isEmpty()) {
            listModel.addElement("<html><i>Nenhuma atividade recente.</i></html>");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            for (Object[] activity : activities) {
                Timestamp timestamp = (Timestamp) activity[0];
                String user = (String) activity[1];
                String entity = (String) activity[2];
                String details = (String) activity[3];
                String formattedTime = timestamp.toLocalDateTime().format(formatter);
                listModel.addElement(String.format("<html><b>%s</b> - %s<br><small><i>por %s em %s</i></small></html>", entity, details, user, formattedTime));
            }
        }
    }

    private static class ActivityCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            return label;
        }
    }
}