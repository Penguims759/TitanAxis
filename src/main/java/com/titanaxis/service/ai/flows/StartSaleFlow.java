package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.util.StringUtil;

import java.util.Map;
import java.util.Optional;

public class StartSaleFlow extends AbstractConversationFlow {

    private final TransactionService transactionService;
    private final ClienteRepository clienteRepository;

    @Inject
    public StartSaleFlow(TransactionService transactionService, ClienteRepository clienteRepository) {
        this.transactionService = transactionService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.START_SALE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        // Tenta obter o nome do cliente que foi pré-extraído pelo AIAssistantService.
        String clientName = (String) conversationData.get("entity");

        if (clientName != null) {
            // Se um nome foi extraído, o fluxo tenta validá-lo e terminar imediatamente.
            return validateAndFinish(clientName, conversationData);
        }

        // Se nenhum cliente foi fornecido na frase inicial, continua com os passos normais.
        return super.process(userInput, conversationData);
    }

    @Override
    protected void defineSteps() {
        steps.put("clientName", new Step(
                "Para qual cliente é a venda? (Opcional, pode deixar em branco)",
                this::isClientNameValid, // A validação agora permite uma string vazia.
                "Cliente não encontrado. Verifique o nome ou deixe em branco para continuar."
        ));
    }

    @Override
    protected AssistantResponse completeFlow(Map<String, Object> conversationData) {
        String clientName = (String) conversationData.get("clientName");
        // Se o utilizador não digitou um nome, o fluxo simplesmente abre o painel de vendas.
        if (clientName == null || clientName.trim().isEmpty()) {
            return new AssistantResponse("Ok, a abrir o painel de vendas.", Action.UI_NAVIGATE, Map.of("destination", "Vendas"));
        }

        // Se um nome foi fornecido, o fluxo valida-o.
        return validateAndFinish(clientName, conversationData);
    }

    private AssistantResponse validateAndFinish(String clientName, Map<String, Object> data) {
        try {
            Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em ->
                    clienteRepository.findByNome(clientName, em));

            if (clienteOpt.isPresent()) {
                data.put("cliente", clienteOpt.get());
                return new AssistantResponse(
                        "Ok, a iniciar a venda para o cliente " + clientName,
                        Action.START_SALE_FOR_CLIENT,
                        data
                );
            } else {
                // Se o cliente não existe, pergunta se o utilizador quer criá-lo.
                // Esta lógica poderia ser expandida para iniciar o CreateClientFlow.
                return new AssistantResponse("Cliente '" + clientName + "' não encontrado. Gostaria de o criar primeiro?");
            }
        } catch (PersistenciaException e) {
            return new AssistantResponse("Ocorreu um erro ao verificar o cliente na base de dados.");
        }
    }

    private boolean isClientNameValid(String name) {
        // Permite que o campo seja vazio para vendas sem cliente definido.
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        // Se um nome for fornecido, verifica se ele existe.
        try {
            return transactionService.executeInTransactionWithResult(em ->
                    clienteRepository.findByNome(name, em)
            ).isPresent();
        } catch (PersistenciaException e) {
            return false;
        }
    }
}