package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.StringUtil;
import java.util.Map;

public class GenerateReportFlow extends AbstractConversationFlow {

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.GENERATE_REPORT;
    }

    @Override
    protected void defineSteps() {
        steps.put("reportType", new Step(
                "Claro. Que tipo de relatório você deseja? (vendas ou inventario)",
                input -> StringUtil.normalize(input).contains("vendas") || StringUtil.normalize(input).contains("inventario"),
                "Por favor, escolha 'vendas' ou 'inventario'."
        ));
        steps.put("format", new Step(
                "E em qual formato? (pdf ou csv)",
                input -> StringUtil.normalize(input).contains("pdf") || StringUtil.normalize(input).contains("csv"),
                "Por favor, escolha 'pdf' ou 'csv'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        // A lógica de geração do relatório em si será tratada pela DashboardFrame
        // através de uma nova Action.
        return new AssistantResponse(
                "Ok, a preparar o seu relatório...",
                Action.DIRECT_GENERATE_SALES_REPORT_PDF, // Podemos reutilizar esta action ou criar uma mais genérica
                conversationData
        );
    }
}