package com.titanaxis.model.dashboard;

import javax.swing.Icon;
import java.awt.Color;

public class Insight {
    public enum InsightType {
        STOCK_ALERT,
        OPPORTUNITY,
        SYSTEM_HABIT;

        // MÉTODO ADICIONADO PARA CORRIGIR O ERRO
        public String getTypeName() {
            return switch (this) {
                case STOCK_ALERT -> "Alertas de Inventário";
                case OPPORTUNITY -> "Oportunidades e Sugestões";
                case SYSTEM_HABIT -> "Hábitos do Sistema";
            };
        }
    }

    private final String text;
    private final InsightType type;
    private final Icon icon;
    private final Color color;
    private final Runnable action;

    public Insight(String text, InsightType type, Icon icon, Color color, Runnable action) {
        this.text = text;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public InsightType getType() {
        return type;
    }

    public Icon getIcon() {
        return icon;
    }

    public Color getColor() {
        return color;
    }

    public Runnable getAction() {
        return action;
    }
}