package com.titanaxis.handler.actions;

import com.titanaxis.view.DashboardFrame;
import java.util.Map;

public class NavigateAction implements DashboardAction {
    private final DashboardFrame frame;

    public NavigateAction(DashboardFrame frame) {
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        String destination = (String) params.get("destination");
        if (destination != null) {
            frame.navigateTo(destination);
        }
    }
}