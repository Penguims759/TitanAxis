package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.UserHabitService;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class CreateManualHabitFlow extends AbstractConversationFlow {

    private final UserHabitService userHabitService;
    private final AuthService authService;

    @Inject
    public CreateManualHabitFlow(UserHabitService userHabitService, AuthService authService) {
        this.userHabitService = userHabitService;
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_MANUAL_HABIT;
    }

    @Override
    protected void defineSteps() {
        steps.put("acao", new Step(
                "Claro. Qual ação você quer que eu sugira? (Ex: 'gerar relatório')",
                (input, data) -> !input.isEmpty(),
                "A ação não pode ser vazia."
        ));
        steps.put("dia", new Step(
                "Em que dia da semana você quer que eu sugira isso?",
                this::isDiaDaSemanaValido,
                "Dia da semana inválido. Por favor, use um dia como 'segunda-feira', 'terça-feira', etc."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        int userId = authService.getUsuarioLogadoId();
        if (userId == 0) {
            return new AssistantResponse("Precisa de estar autenticado para criar um hábito.");
        }

        try {
            String acao = (String) conversationData.get("acao");
            DayOfWeek dia = parseDiaDaSemana((String) conversationData.get("dia"));

            userHabitService.createManualHabit(userId, acao.toUpperCase().replace(" ", "_"), dia);

            String diaFormatado = dia.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            return new AssistantResponse(String.format("Hábito criado! Irei sugerir a ação '%s' todas as %s.", acao, diaFormatado));

        } catch (Exception e) {
            return new AssistantResponse("Ocorreu um erro ao criar o hábito: " + e.getMessage());
        }
    }

    private boolean isDiaDaSemanaValido(String input) {
        return parseDiaDaSemana(input) != null;
    }

    private DayOfWeek parseDiaDaSemana(String input) {
        String lowerInput = input.toLowerCase().replace("-feira", "");
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")).toLowerCase().startsWith(lowerInput)) {
                return day;
            }
        }
        return null;
    }
}