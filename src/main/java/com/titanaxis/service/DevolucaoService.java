// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/service/DevolucaoService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.*;
import com.titanaxis.repository.DevolucaoRepository;
import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.EntityManager;

public class DevolucaoService {

    private final DevolucaoRepository devolucaoRepository;
    private final TransactionService transactionService;

    @Inject
    public DevolucaoService(DevolucaoRepository devolucaoRepository, TransactionService transactionService) {
        this.devolucaoRepository = devolucaoRepository;
        this.transactionService = transactionService;
    }

    public void registrarDevolucao(Devolucao devolucao, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        devolucao.setUsuario(ator);

        transactionService.executeInTransaction(em -> {
            // Salva a devolução
            Devolucao devolucaoSalva = devolucaoRepository.save(devolucao, ator, em);

            // Restaura o estoque e cria o movimento
            for (DevolucaoItem itemDevolvido : devolucao.getItens()) {
                VendaItem vendaItemOriginal = itemDevolvido.getVendaItem();
                Lote lote = vendaItemOriginal.getLote();
                lote.setQuantidade(lote.getQuantidade() + itemDevolvido.getQuantidadeDevolvida());
                em.merge(lote);

                criarMovimentoEstoque(em, ator, lote, itemDevolvido.getQuantidadeDevolvida(), devolucaoSalva.getId());
            }

            // Adiciona crédito ao cliente, se houver
            if (devolucao.getVenda().getCliente() != null && devolucao.getValorEstornado() > 0) {
                Cliente cliente = em.find(Cliente.class, devolucao.getVenda().getCliente().getId());
                cliente.adicionarCredito(devolucao.getValorEstornado());
                em.merge(cliente);
            }
        });
    }

    private void criarMovimentoEstoque(EntityManager em, Usuario ator, Lote lote, int quantidade, int devolucaoId) {
        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setProduto(lote.getProduto());
        movimento.setLote(lote);
        movimento.setTipoMovimento("DEVOLUCAO");
        movimento.setQuantidade(quantidade);
        movimento.setUsuario(ator);
        // Opcional: Adicionar um campo "devolucao_id" na tabela de movimentos para rastreabilidade
        em.persist(movimento);
    }
}