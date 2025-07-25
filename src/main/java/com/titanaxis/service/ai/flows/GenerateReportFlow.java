package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.Intent;
import com.titanaxis.util.I18n;
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
                I18n.getString("flow.generateReport.askType"),
                input -> StringUtil.normalize(input).contains("vendas") || StringUtil.normalize(input).contains("inventario"),
                I18n.getString("flow.generateReport.validation.invalidType")
        ));
        steps.put("format", new Step(
                I18n.getString("flow.generateReport.askFormat"),
                input -> StringUtil.normalize(input).contains("pdf") || StringUtil.normalize(input).contains("csv"),
                I18n.getString("flow.generateReport.validation.invalidFormat")
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        return new AssistantResponse(
                I18n.getString("flow.generateReport.generating"),
                Action.DIRECT_GENERATE_SALES_REPORT_PDF,
                conversationData
        );
    }
}