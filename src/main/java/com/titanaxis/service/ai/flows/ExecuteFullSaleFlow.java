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
import com.titanaxis.util.I18n;

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
        String productName = (String) data.get("produto");
        String lotNumber = (String) data.get("lote");
        String clientName = (String) data.get("cliente");
        Integer quantity = (Integer) data.get("quantidade");

        if (productName == null || quantity == null) {
            return new AssistantResponse(I18n.getString("flow.executeSale.error.missingInfo"));
        }
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (ator == null) {
            return new AssistantResponse(I18n.getString("flow.generic.error.authRequired"));
        }

        try {
            Optional<Produto> produtoOpt = transactionService.executeInTransactionWithResult(em -> produtoRepository.findByNome(productName, em));
            if (produtoOpt.isEmpty()) {
                return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", productName));
            }
            Produto produto = produtoOpt.get();

            Optional<Lote> loteOpt;
            if (lotNumber != null) {
                loteOpt = produto.getLotes().stream().filter(l -> l.getNumeroLote().equalsIgnoreCase(lotNumber)).findFirst();
                if (loteOpt.isEmpty()) {
                    return new AssistantResponse(I18n.getString("flow.executeSale.error.lotNotFound", lotNumber, productName));
                }
            } else {
                loteOpt = produto.getLotes().stream().filter(l -> l.getQuantidade() >= quantity).findFirst();
                if (loteOpt.isEmpty()) {
                    return new AssistantResponse(I18n.getString("flow.executeSale.error.noLotWithStock", productName, produto.getQuantidadeTotal()));
                }
            }
            Lote lote = loteOpt.get();

            if (lote.getQuantidade() < quantity) {
                return new AssistantResponse(I18n.getString("flow.executeSale.error.insufficientStock", lote.getNumeroLote(), productName, lote.getQuantidade()));
            }

            Cliente cliente = null;
            if (clientName != null) {
                Optional<Cliente> clienteOpt = transactionService.executeInTransactionWithResult(em -> clienteRepository.findByNome(clientName, em));
                if (clienteOpt.isEmpty()) {
                    return new AssistantResponse(I18n.getString("flow.generic.error.entityNotFound", clientName));
                }
                cliente = clienteOpt.get();
            }

            Carrinho carrinho = new Carrinho(ator);
            carrinho.adicionarItem(lote, quantity);
            if (cliente != null) {
                carrinho.setCliente(cliente);
            }

            Venda vendaFinalizada = vendaService.finalizarVenda(carrinho.getVenda(), ator);

            String clienteInfo = cliente != null ? " " + I18n.getString("flow.executeSale.forClient", cliente.getNome()) : "";
            String responseText = I18n.getString("flow.executeSale.success",
                    vendaFinalizada.getId(),
                    clienteInfo,
                    NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(vendaFinalizada.getValorTotal())
            );
            return new AssistantResponse(responseText);

        } catch (PersistenciaException | UtilizadorNaoAutenticadoException | CarrinhoVazioException |
                 IllegalArgumentException e) {
            return new AssistantResponse(I18n.getString("flow.executeSale.error.generic", e.getMessage()));
        }
    }
}