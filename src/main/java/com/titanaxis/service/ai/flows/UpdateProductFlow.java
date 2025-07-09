package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.util.StringUtil;

import java.util.Map;

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
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        // Verifica se a entidade (nome do produto) já veio do contexto
        if (conversationData.get("entity") != null && !conversationData.containsKey("productName")) {
            if(isProdutoValido((String) conversationData.get("entity"))){
                conversationData.put("productName", conversationData.get("entity"));
            }
        }

        // Primeiro, executa a lógica padrão de recolha de dados
        AssistantResponse response = super.process(userInput, conversationData);

        // Se a resposta indica que ainda estamos a aguardar informação, retornamo-la diretamente
        if (response.getAction() == Action.AWAITING_INFO) {
            return response;
        }

        // Se chegámos aqui, significa que os passos principais foram concluídos e temos de lidar com os sub-fluxos
        String updateType = StringUtil.normalize((String) conversationData.get("updateType"));

        if (updateType.contains("preco")) {
            return handlePriceUpdate(userInput, conversationData);
        } else {
            return handleStatusUpdate(userInput, conversationData);
        }
    }

    private AssistantResponse handlePriceUpdate(String userInput, Map<String, Object> data) {
        if (!data.containsKey("newPrice")) {
            if (!userInput.isEmpty() && StringUtil.isNumeric(userInput.replace(",", "."))) {
                data.put("newPrice", Double.parseDouble(userInput.replace(",", ".")));
            } else {
                return new AssistantResponse("Qual o novo preço?", Action.AWAITING_INFO, null);
            }
        }

        String confirmationMessage = String.format("Você confirma a alteração do preço do produto %s para %.2f?", data.get("productName"), data.get("newPrice"));
        data.put("flow", "CONFIRM_PRODUCT_UPDATE");
        return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
    }

    private AssistantResponse handleStatusUpdate(String userInput, Map<String, Object> data) {
        if (!data.containsKey("active")) {
            String normalizedInput = StringUtil.normalize(userInput);
            if (normalizedInput.contains("ativar")) data.put("active", true);
            else if (normalizedInput.contains("inativar")) data.put("active", false);
            else {
                return new AssistantResponse("Deseja 'ativar' ou 'inativar' o produto?", Action.AWAITING_INFO, null);
            }
        }

        String confirmationMessage = String.format("Você confirma a %s do produto %s?", ((Boolean) data.get("active") ? "ativação" : "inativação"), data.get("productName"));
        data.put("flow", "CONFIRM_PRODUCT_UPDATE");
        return new AssistantResponse(confirmationMessage, Action.AWAITING_INFO, null);
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        if ("sim".equalsIgnoreCase((String) conversationData.get("confirmation"))) {
            return new AssistantResponse("Ok, a atualizar o produto...", Action.DIRECT_UPDATE_PRODUCT, conversationData);
        }
        return new AssistantResponse("Ação cancelada.");
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