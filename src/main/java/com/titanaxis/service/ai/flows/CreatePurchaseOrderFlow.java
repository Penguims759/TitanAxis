package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.FornecedorRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.util.StringUtil;

import java.util.Map;

public class CreatePurchaseOrderFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    @Inject
    public CreatePurchaseOrderFlow(TransactionService transactionService, FornecedorRepository fornecedorRepository, ProdutoRepository produtoRepository) {
        this.transactionService = transactionService;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.CREATE_PURCHASE_ORDER;
    }

    @Override
    protected void defineSteps() {
        steps.put("fornecedor", new Step(
                "Claro, vamos criar uma ordem de compra. Para qual fornecedor?",
                this::isFornecedorValido,
                "Fornecedor não encontrado. Por favor, verifique o nome."
        ));
        steps.put("produto", new Step(
                "Qual produto você deseja pedir?",
                this::isProdutoValido,
                "Produto não encontrado. Por favor, verifique o nome."
        ));
        steps.put("quantidade", new Step(
                "Qual a quantidade a ser pedida?",
                StringUtil::isNumeric,
                "A quantidade deve ser um número."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        // Por enquanto, esta ação é simulada. No futuro, poderia criar um registo na BD.
        String responseText = String.format("Ok, registei um rascunho de pedido de compra de %s unidades de '%s' para o fornecedor '%s'.",
                conversationData.get("quantidade"),
                conversationData.get("produto"),
                conversationData.get("fornecedor")
        );
        return new AssistantResponse(responseText);
    }

    private boolean isFornecedorValido(String nomeFornecedor) {
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    fornecedorRepository.findByNome(nomeFornecedor, em).isPresent()
            );
        } catch (PersistenciaException e) {
            return false;
        }
    }

    private boolean isProdutoValido(String nomeProduto) {
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    produtoRepository.findByNome(nomeProduto, em).isPresent()
            );
        } catch (PersistenciaException e) {
            return false;
        }
    }
}