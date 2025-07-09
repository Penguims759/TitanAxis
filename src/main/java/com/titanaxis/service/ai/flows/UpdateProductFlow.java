// Caminho: penguims759/titanaxis/Penguims759-TitanAxis-d11978d74c8d39dd19a6d1a7bb798e37ccb09060/src/main/java/com/titanaxis/service/ai/flows/UpdateProductFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.Optional;

public class UpdateProductFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final ProdutoRepository produtoRepository;

    @Inject
    public UpdateProductFlow(TransactionService transactionService, ProdutoRepository produtoRepository) {
        this.transactionService = transactionService;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.UPDATE_PRODUCT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (conversationData.get("entity") != null && !conversationData.containsKey("productName")) {
            String productNameFromContext = (String) conversationData.get("entity");
            if(isProdutoValido(productNameFromContext)){
                conversationData.put("productName", productNameFromContext);
                // Guarda a entidade encontrada para manter o contexto se o fluxo for concluído
                try {
                    transactionService.executeInTransactionWithResult(em -> produtoRepository.findByNome(productNameFromContext, em))
                            .ifPresent(p -> conversationData.put("foundEntity", p));
                } catch (PersistenciaException e) { /* Ignora o erro de contexto */ }
            }
        }

        // Lógica dos sub-fluxos (preço, status)
        String flowStep = (String) conversationData.get("flow");
        if ("PRICE_UPDATE".equals(flowStep)) {
            return handlePriceUpdate(userInput, conversationData);
        } else if ("STATUS_UPDATE".equals(flowStep)) {
            return handleStatusUpdate(userInput, conversationData);
        } else if ("CONFIRM_UPDATE".equals(flowStep)) {
            if (userInput.equalsIgnoreCase("sim")) {
                return completeFlow(conversationData);
            } else {
                conversationData.clear();
                return new AssistantResponse("Ok, ação cancelada.");
            }
        }

        return super.process(userInput, conversationData);
    }


    @Override
    protected void defineSteps() {
        steps.put("productName", new Step(
                "Qual produto você deseja alterar?",
                this::isProdutoValido,
                "Não encontrei este produto. Por favor, verifique o nome."
        ));

        steps.put("updateType", new Step(
                data -> "O que você deseja alterar no produto '" + data.get("productName") + "'? (preço ou status)",
                input -> StringUtil.normalize(input).contains("preco") || StringUtil.normalize(input).contains("status"),
                "Não entendi. Você pode alterar o 'preço' ou o 'status'."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String updateType = StringUtil.normalize((String) conversationData.get("updateType"));
        if(updateType.contains("preco")){
            conversationData.put("flow", "PRICE_UPDATE");
            return new AssistantResponse("Qual o novo preço?", Action.AWAITING_INFO, null);
        } else {
            conversationData.put("flow", "STATUS_UPDATE");
            return new AssistantResponse("Deseja 'ativar' ou 'inativar' o produto?", Action.AWAITING_INFO, null);
        }
    }

    private AssistantResponse handlePriceUpdate(String userInput, Map<String, Object> data) {
        if (StringUtil.isNumeric(userInput.replace(",", "."))) {
            data.put("newPrice", Double.parseDouble(userInput.replace(",", ".")));
            String confirmationMessage = String.format("Você confirma a alteração do preço do produto %s para %.2f? (sim/não)", data.get("productName"), data.get("newPrice"));
            data.put("flow", "CONFIRM_UPDATE");
            return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
        }
        return new AssistantResponse("Preço inválido. Por favor, digite um número.", Action.AWAITING_INFO, null);
    }

    private AssistantResponse handleStatusUpdate(String userInput, Map<String, Object> data) {
        String normalizedInput = StringUtil.normalize(userInput);
        if (normalizedInput.contains("ativar")) data.put("active", true);
        else if (normalizedInput.contains("inativar")) data.put("active", false);
        else {
            return new AssistantResponse("Não entendi. Deseja 'ativar' ou 'inativar'?", Action.AWAITING_INFO, null);
        }
        String confirmationMessage = String.format("Você confirma a %s do produto %s? (sim/não)", ((Boolean) data.get("active") ? "ativação" : "inativação"), data.get("productName"));
        data.put("flow", "CONFIRM_UPDATE");
        return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
    }

    private boolean isProdutoValido(String nomeProduto) {
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    produtoRepository.findByNome(nomeProduto, em)
            ).isPresent();
        } catch (PersistenciaException e) {
            return false;
        }
    }
}