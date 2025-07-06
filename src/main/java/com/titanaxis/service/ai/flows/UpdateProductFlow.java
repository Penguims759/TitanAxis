// src/main/java/com/titanaxis/service/ai/flows/UpdateProductFlow.java
package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.AIAssistantService.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.ConversationFlow;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.Optional;

public class UpdateProductFlow implements ConversationFlow {

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
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        // Se recebemos uma entrada do utilizador, ela é a resposta à nossa última pergunta.
        // Vamos processá-la e guardá-la no mapa 'data'.
        if (!userInput.isEmpty()) {
            if (!data.containsKey("productName")) {
                try {
                    final String productName = userInput;
                    Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(em -> produtoRepository.findByNome(productName, em));
                    if (produtoOpt.isEmpty()) {
                        return new AssistantResponse("Não encontrei o produto '" + productName + "'. Qual produto você deseja alterar?");
                    }
                    data.put("productName", productName);
                } catch (PersistenciaException e) {
                    return new AssistantResponse("Ocorreu um erro ao buscar o produto. Tente novamente.");
                }
            } else if (!data.containsKey("updateType")) {
                if (StringUtil.normalize(userInput).contains("preco")) data.put("updateType", "price");
                else if (StringUtil.normalize(userInput).contains("status")) data.put("updateType", "status");
                else return new AssistantResponse("Não entendi. Você pode alterar o 'preço' ou o 'status'.");
            } else {
                String updateType = (String) data.get("updateType");
                if ("price".equals(updateType) && !data.containsKey("newPrice")) {
                    try {
                        data.put("newPrice", Double.parseDouble(userInput.replace(",", ".")));
                    } catch (NumberFormatException e) {
                        return new AssistantResponse("Preço inválido. Por favor, digite um número.");
                    }
                } else if ("status".equals(updateType) && !data.containsKey("active")) {
                    String normalizedInput = StringUtil.normalize(userInput);
                    if (normalizedInput.contains("ativar")) data.put("active", true);
                    else if (normalizedInput.contains("inativar")) data.put("active", false);
                    else return new AssistantResponse("Não entendi. Por favor, responda com 'ativar' ou 'inativar'.");
                }
            }
        }

        // Depois de processar a entrada, verificamos qual é o próximo dado que falta e fazemos a pergunta.
        if (!data.containsKey("productName")) {
            return new AssistantResponse("Qual produto você deseja alterar?");
        }
        if (!data.containsKey("updateType")) {
            return new AssistantResponse("O que você deseja alterar no produto '" + data.get("productName") + "'? (preço ou status)");
        }

        String updateType = (String) data.get("updateType");
        if ("price".equals(updateType) && !data.containsKey("newPrice")) {
            return new AssistantResponse("Qual o novo preço?");
        }
        if ("status".equals(updateType) && !data.containsKey("active")) {
            return new AssistantResponse("Deseja 'ativar' ou 'inativar' o produto?");
        }

        // Se todos os dados foram recolhidos, montamos e retornamos a mensagem de confirmação.
        data.put("flow", "CONFIRM_PRODUCT_UPDATE");
        String confirmationMessage;
        if ("price".equals(updateType)) {
            confirmationMessage = String.format("Você confirma a alteração do preço do produto %s para %.2f?", data.get("productName"), data.get("newPrice"));
        } else {
            confirmationMessage = String.format("Você confirma a %s do produto %s?", ((Boolean) data.get("active") ? "ativação" : "inativação"), data.get("productName"));
        }

        return new AssistantResponse(confirmationMessage);
    }
}