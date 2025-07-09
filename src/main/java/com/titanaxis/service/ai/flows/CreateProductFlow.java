package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Produto; // Import necessário
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateProductFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final CategoriaRepository categoriaRepository;

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
    protected void defineSteps() {
        steps.put("nome", new Step("Ok, vamos criar um produto. Qual o nome dele?"));
        steps.put("preco", new Step(
                data -> "Qual o preço de venda para '" + data.get("nome") + "'?",
                StringUtil::isNumeric,
                "Preço inválido. Por favor, digite um número (ex: 99.90)."
        ));
        steps.put("categoria", new Step(
                "A qual categoria este produto pertence?",
                this::isCategoriaValida,
                "Categoria não encontrada. Verifique o nome ou crie a categoria primeiro."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        try {
            String nome = (String) conversationData.get("nome");
            double preco = Double.parseDouble(((String) conversationData.get("preco")).replace(",", "."));
            String nomeCategoria = (String) conversationData.get("categoria");

            Categoria categoria = transactionService.executeInTransactionWithResult(em ->
                    categoriaRepository.findByNome(nomeCategoria, em)
            ).orElseThrow(() -> new IllegalStateException("A categoria validada não foi encontrada."));

            // Cria um objeto Produto para passar para a ação
            Produto novoProduto = new Produto(nome, "", preco, categoria);

            Map<String, Object> actionParams = new HashMap<>();
            actionParams.put("nome", nome);
            actionParams.put("preco", preco);
            actionParams.put("categoria", categoria);

            // NOVO: Adiciona a entidade criada para que seja guardada no contexto
            conversationData.put("foundEntity", novoProduto);

            return new AssistantResponse(
                    "Ok, a criar o produto '" + nome + "'...",
                    Action.DIRECT_CREATE_PRODUCT,
                    actionParams
            );
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro na base de dados ao finalizar a criação do produto.");
        }
    }

    private boolean isCategoriaValida(String nomeCategoria) {
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    categoriaRepository.findByNome(nomeCategoria, em)
            ).isPresent();
        } catch (PersistenciaException e) {
            return false;
        }
    }
}