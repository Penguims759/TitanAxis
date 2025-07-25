package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.UserHabitService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryUserHabitsFlow implements ConversationFlow {

    private final UserHabitService userHabitService;
    private final AuthService authService;

    @Inject
    public QueryUserHabitsFlow(UserHabitService userHabitService, AuthService authService) {
        this.userHabitService = userHabitService;
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_USER_HABITS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        int userId = authService.getUsuarioLogadoId();
        if (userId == 0) {
            return new AssistantResponse(I18n.getString("flow.generic.error.authRequired"));
        }

        try {
            List<Habito> habitos = userHabitService.findHabitsForUser(userId);
            if (habitos.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryHabits.noHabits"));
            }

            String habitosFormatados = habitos.stream()
                    .map(h -> I18n.getString("flow.queryHabits.habitLine",
                            h.getAcao(),
                            h.getDiaDaSemana().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")),
                            h.getTipo().toString().toLowerCase()
                    ))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse(I18n.getString("flow.queryHabits.header") + "\n" + habitosFormatados);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryHabits.error.generic"));
        }
    }
}