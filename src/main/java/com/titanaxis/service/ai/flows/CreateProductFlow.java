package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Produto;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.ai.FlowValidationService;
import com.titanaxis.util.I18n;
import com.titanaxis.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class CreateProductFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final CategoriaRepository categoriaRepository;
    private final FlowValidationService validationService;

    @Inject
    public CreateProductFlow(TransactionService transactionService, CategoriaRepository categoriaRepository, FlowValidationService validationService) {
        this.transactionService = transactionService;
        this.categoriaRepository = categoriaRepository;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_PRODUCT;
    }

    @Override
    protected void defineSteps() {
        steps.put("nome", new Step(I18n.getString("flow.createProduct.askName")));
        steps.put("preco", new Step(
                data -> I18n.getString("flow.createProduct.askPrice", data.get("nome")),
                StringUtil::isNumeric,
                I18n.getString("flow.validation.invalidNumber")
        ));
        steps.put("categoria", new Step(
                I18n.getString("flow.createProduct.askCategory"),
                (input, data) -> validationService.isCategoriaValida(input),
                I18n.getString("flow.createProduct.validation.categoryNotFound")
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

            Produto novoProduto = new Produto(nome, "", preco, categoria);

            Map<String, Object> actionParams = new HashMap<>();
            actionParams.put("nome", nome);
            actionParams.put("preco", preco);
            actionParams.put("categoria", categoria);

            conversationData.put("foundEntity", novoProduto);

            return new AssistantResponse(
                    I18n.getString("flow.createProduct.creating", nome),
                    Action.DIRECT_CREATE_PRODUCT,
                    actionParams
            );
        } catch (PersistenciaException e) {
            return new AssistantResponse(I18n.getString("flow.createProduct.error.finalizing"));
        }
    }
}