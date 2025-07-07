package com.titanaxis.model.ai;

import java.util.Map;

public class AssistantResponse {

    private final String textResponse;
    private final Action action;
    private final Map<String, Object> actionParams;

    public AssistantResponse(String textResponse) {
        this(textResponse, null, null);
    }

    public AssistantResponse(String textResponse, Action action, Map<String, Object> actionParams) {
        this.textResponse = textResponse;
        this.action = action;
        this.actionParams = actionParams;
    }

    public String getTextResponse() {
        return textResponse;
    }

    public Action getAction() {
        return action;
    }

    public Map<String, Object> getActionParams() {
        return actionParams;
    }

    public boolean hasAction() {
        return action != null;
    }
}