package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.AlertaService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.I18n;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryExpiringLotsFlow implements ConversationFlow {

    private final AlertaService alertaService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Inject
    public QueryExpiringLotsFlow(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.QUERY_EXPIRING_LOTS;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        try {
            List<Lote> lotes = alertaService.getLotesProximosDoVencimento();

            if (lotes.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.queryExpiring.noLots"));
            }

            String lotesList = lotes.stream()
                    .map(lote -> I18n.getString("flow.queryExpiring.lotLine",
                            lote.getProduto().getNome(),
                            lote.getNumeroLote(),
                            lote.getDataValidade().format(DATE_FORMATTER)))
                    .collect(Collectors.joining("\n"));

            return new AssistantResponse(I18n.getString("flow.queryExpiring.header") + "\n" + lotesList);

        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.queryExpiring.error.generic"));
        }
    }
}