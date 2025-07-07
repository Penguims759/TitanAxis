package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import java.util.Map;
import java.util.Optional;

public class CreateProductFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final CategoriaRepository categoriaRepository;

    private enum State {
        START, AWAITING_NAME, AWAITING_PRICE, AWAITING_CATEGORY
    }

    @Inject
    public CreateProductFlow(TransactionService transactionService, CategoriaRepository categoriaRepository) {
        this.transactionService = transactionService;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_PRODUCT;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        State currentState = (State) data.getOrDefault("state", State.START);

        if (currentState != State.START && !userInput.isEmpty()) {
            switch (currentState) {
                case AWAITING_NAME:
                    data.put("nome", userInput);
                    break;
                case AWAITING_PRICE:
                    try {
                        data.put("preco", Double.parseDouble(userInput.replace(",", ".")));
                    } catch (NumberFormatException e) {
                        return new AssistantResponse("Preço inválido. Por favor, digite um número (ex: 99.90).");
                    }
                    break;
                case AWAITING_CATEGORY:
                    try {
                        Optional<Categoria> catOpt = transactionService.executeInTransactionWithResult(em -> categoriaRepository.findByNome(userInput, em));
                        if (catOpt.isPresent()) {
                            data.put("categoria", catOpt.get());
                        } else {
                            return new AssistantResponse("Categoria '" + userInput + "' não encontrada. Verifique o nome ou crie a categoria primeiro.");
                        }
                    } catch (PersistenciaException e) {
                        return new AssistantResponse("Ocorreu um erro ao buscar as categorias. Tente novamente.");
                    }
                    break;
            }
        }

        if (!data.containsKey("nome")) {
            data.put("state", State.AWAITING_NAME);
            return new AssistantResponse("Ok, vamos criar um produto. Qual o nome dele?");
        }
        if (!data.containsKey("preco")) {
            data.put("state", State.AWAITING_PRICE);
            return new AssistantResponse("Qual o preço de venda para '" + data.get("nome") + "'?");
        }
        if (!data.containsKey("categoria")) {
            data.put("state", State.AWAITING_CATEGORY);
            return new AssistantResponse("A qual categoria este produto pertence?");
        }

        data.put("isFinal", true);
        return new AssistantResponse("Ok, a criar o produto '" + data.get("nome") + "'...", Action.DIRECT_CREATE_PRODUCT, data);
    }
}