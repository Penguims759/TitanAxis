package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.util.I18n;

import java.util.Arrays;
import java.util.Map;

public class CreateUserFlow extends AbstractConversationFlow {

    private final AuthService authService;

    @Inject
    public CreateUserFlow(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_USER;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (!authService.isGerente()) {
            return new AssistantResponse(I18n.getString("flow.createUser.error.permissionDenied"));
        }
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("username", new Step(I18n.getString("flow.createUser.askUsername")));
        steps.put("password", new Step(data -> I18n.getString("flow.createUser.askPassword", data.get("username"))));
        steps.put("level", new Step(
                I18n.getString("flow.createUser.askLevel"),
                (input, data) -> isNivelAcessoValido(input),
                I18n.getString("flow.createUser.validation.invalidLevel")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        NivelAcesso nivel = NivelAcesso.valueOf(((String) conversationData.get("level")).trim().toUpperCase());
        conversationData.put("level", nivel);

        return new AssistantResponse(
                I18n.getString("flow.createUser.creating", conversationData.get("username")),
                Action.DIRECT_CREATE_USER,
                conversationData
        );
    }

    private boolean isNivelAcessoValido(String input) {
        return Arrays.stream(NivelAcesso.values())
                .anyMatch(nivel -> nivel.name().equalsIgnoreCase(input.trim()));
    }
}