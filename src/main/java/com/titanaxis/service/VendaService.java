// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/service/VendaService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.CarrinhoVazioException; // Importado
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.repository.VendaRepository;

public class VendaService {

    private final VendaRepository vendaRepository;
    private final TransactionService transactionService;

    @Inject
    public VendaService(VendaRepository vendaRepository, TransactionService transactionService) {
        this.vendaRepository = vendaRepository;
        this.transactionService = transactionService;
    }

    // ALTERADO: Assinatura do método para lançar exceções específicas
    public Venda finalizarVenda(Venda venda, Usuario ator) throws UtilizadorNaoAutenticadoException, CarrinhoVazioException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new CarrinhoVazioException("A venda não contém itens."); // ALTERADO: Lança CarrinhoVazioException
        }

        // ALTERADO: O bloco try-catch de RuntimeException foi removido,
        // pois TransactionService agora encapsula tudo em PersistenciaException
        return transactionService.executeInTransactionWithResult(em -> {
            for (VendaItem item : venda.getItens()) {
                Lote lote = em.find(Lote.class, item.getLote().getId());
                if (lote == null || lote.getQuantidade() < item.getQuantidade()) {
                    // Lança RuntimeException que será encapsulada em PersistenciaException pelo TransactionService
                    throw new RuntimeException("Estoque insuficiente para o produto: " + item.getLote().getProduto().getNome());
                }
                lote.setQuantidade(lote.getQuantidade() - item.getQuantidade());
            }
            return vendaRepository.save(venda, ator, em);
        });
    }
}