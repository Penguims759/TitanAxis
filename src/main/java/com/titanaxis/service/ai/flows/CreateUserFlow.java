package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;

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
        if (!authService.isGerente()) { // Apenas Gerentes ou Admins podem criar utilizadores
            return new AssistantResponse("Desculpe, apenas gestores ou administradores podem criar novos utilizadores.");
        }
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("username", new Step("Certo. Qual o nome do novo utilizador?"));
        steps.put("password", new Step(data -> "Qual será a senha para '" + data.get("username") + "'?"));
        steps.put("level", new Step(
                "E qual o nível de acesso? (padrao, gerente, ou admin)",
                this::isNivelAcessoValido,
                "Nível inválido. Use 'padrao', 'gerente' ou 'admin'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        // Converte o nível de acesso de String para o Enum antes de enviar
        NivelAcesso nivel = NivelAcesso.valueOf(((String) conversationData.get("level")).trim().toUpperCase());
        conversationData.put("level", nivel);

        return new AssistantResponse(
                "A criar o utilizador '" + conversationData.get("username") + "'...",
                Action.DIRECT_CREATE_USER,
                conversationData
        );
    }

    private boolean isNivelAcessoValido(String input) {
        return Arrays.stream(NivelAcesso.values())
                .anyMatch(nivel -> nivel.name().equalsIgnoreCase(input.trim()));
    }
}