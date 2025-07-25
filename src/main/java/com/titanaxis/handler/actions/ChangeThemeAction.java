package com.titanaxis.handler.actions;

import com.titanaxis.view.DashboardFrame;
import java.util.Map;

public class ChangeThemeAction implements DashboardAction {
    private final DashboardFrame frame;

    public ChangeThemeAction(DashboardFrame frame) {
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        String theme = (String) params.get("theme");
        if (theme != null) {
            frame.setTheme(theme);
        }
    }
}