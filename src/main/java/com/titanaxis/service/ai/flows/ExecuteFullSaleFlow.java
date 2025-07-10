package com.titanaxis.service.ai.flows;

import com.google.inject.Inject;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.*;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.repository.ClienteRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.Intent;
import com.titanaxis.service.TransactionService;
import com.titanaxis.service.VendaService;
import com.titanaxis.service.ai.ConversationFlow;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ExecuteFullSaleFlow implements ConversationFlow {

    private final TransactionService transactionService;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final VendaService vendaService;
    private final AuthService authService;

    @Inject
    public ExecuteFullSaleFlow(TransactionService transactionService, ProdutoRepository produtoRepository, ClienteRepository clienteRepository, VendaService vendaService, AuthService authService) {
        this.transactionService = transactionService;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.vendaService = vendaService;
        this.authService = authService;
    }

    @Override
    public boolean canHandle(Intent intent) {
        return intent == Intent.EXECUTE_FULL_SALE;
    }

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> data) {
        // Extrair as entidades do mapa 'data' que foi preenchido pelo AIAssistantService
        String productName = (String) data.get("produto");
        String lotNumber = (String) data.get("lote");
        String clientName = (String) data.get("cliente");
        Integer quantity = (Integer) data.get("quantidade");

        // Validações
        if (productName == null || quantity == null) {
            return new AssistantResponse("Para realizar a venda, preciso saber pelo menos o produto e a quantidade. Ex: 'vender 2 canetas'.");
        }
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (ator == null) {
            return new AssistantResponse("Não consigo realizar a venda pois nenhum utilizador está autenticado.");
        }

        try {
            // Valida o produto
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(em -> produtoRepository.findByNome(productName, em));
            if (produtoOpt.isEmpty()) {
                return new AssistantResponse("Produto '" + productName + "' não encontrado.");
            }
            Produto produto = produtoOpt.get();

            // Valida o Lote
            Optional<Lote> loteOpt;
            if (lotNumber != null) {
                loteOpt = produto.getLotes().stream().filter(l -> l.getNumeroLote().equalsIgnoreCase(lotNumber)).findFirst();
                if (loteOpt.isEmpty()) {
                    return new AssistantResponse(String.format("O lote '%s' não foi encontrado para o produto '%s'.", lotNumber, productName));
                }
            } else {
                // Se nenhum lote foi especificado, pega o primeiro lote disponível com estoque
                loteOpt = produto.getLotes().stream().filter(l -> l.getQuantidade() >= quantity).findFirst();
                if (loteOpt.isEmpty()) {
                    return new AssistantResponse(String.format("Não há lotes com estoque suficiente para '%s'. O estoque total é de %d unidades.", productName, produto.getQuantidadeTotal()));
                }
            }
            Lote lote = loteOpt.get();

            // Valida a quantidade no lote
            if (lote.getQuantidade() < quantity) {
                return new AssistantResponse(String.format("O lote '%s' do produto '%s' possui apenas %d unidades em estoque.", lote.getNumeroLote(), productName, lote.getQuantidade()));
            }

            // Valida o cliente (se especificado)
            Cliente cliente = null;
            if (clientName != null) {
                Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em -> clienteRepository.findByNome(clientName, em));
                if (clienteOpt.isEmpty()) {
                    return new AssistantResponse("Cliente '" + clientName + "' não encontrado.");
                }
                cliente = clienteOpt.get();
            }

            // Cria e finaliza a venda
            Carrinho carrinho = new Carrinho(ator);
            carrinho.adicionarItem(lote, quantity);
            if (cliente != null) {
                carrinho.setCliente(cliente);
            }

            Venda vendaFinalizada = vendaService.finalizarVenda(carrinho.getVenda(), ator);

            String clienteInfo = cliente != null ? " para o cliente '" + cliente.getNome() + "'" : "";
            String responseText = String.format("Venda #%d realizada com sucesso%s, no valor de %s.",
                    vendaFinalizada.getId(),
                    clienteInfo,
                    NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(vendaFinalizada.getValorTotal())
            );
            return new AssistantResponse(responseText);

        } catch (PersistenciaException | UtilizadorNaoAutenticadoException | CarrinhoVazioException |
                 IllegalArgumentException e) {
            return new AssistantResponse("Ocorreu um erro ao processar a venda: " + e.getMessage());
        }
    }
}