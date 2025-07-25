package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.UserHabitService;
import com.titanaxis.util.I18n;

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
                I18n.getString("flow.createHabit.askAction"),
                (input, data) -> !input.isEmpty(),
                I18n.getString("flow.validation.requiredField")
        ));
        steps.put("dia", new Step(
                I18n.getString("flow.createHabit.askDay"),
                (input, data) -> isDiaDaSemanaValido(input),
                I18n.getString("flow.createHabit.validation.invalidDay")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        int userId = authService.getUsuarioLogadoId();
        if (userId == 0) {
            return new AssistantResponse(I18n.getString("flow.generic.error.authRequired"));
        }

        try {
            String acao = (String) conversationData.get("acao");
            DayOfWeek dia = parseDiaDaSemana((String) conversationData.get("dia"));

            userHabitService.createManualHabit(userId, acao.toUpperCase().replace(" ", "_"), dia);

            String diaFormatado = dia.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            return new AssistantResponse(I18n.getString("flow.createHabit.success", acao, diaFormatado));

        } catch (Exception e) {
            return new AssistantResponse(I18n.getString("flow.createHabit.error.generic", e.getMessage()));
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