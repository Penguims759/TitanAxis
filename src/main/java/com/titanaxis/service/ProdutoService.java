package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.MovimentoEstoque; // Importação necessária
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.ProdutoRepository;

import java.time.LocalDateTime; // Importação necessária
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;

    @Inject
    public ProdutoService(ProdutoRepository produtoRepository, TransactionService transactionService) {
        this.produtoRepository = produtoRepository;
        this.transactionService = transactionService;
    }

    // ... (outros métodos do serviço permanecem iguais) ...

    public List<Produto> listarProdutos(boolean incluirInativos) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            if (incluirInativos) {
                return produtoRepository.findAllIncludingInactive(em);
            }
            return produtoRepository.findAll(em);
        });
    }

    public List<Produto> listarProdutosAtivosParaVenda() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAll(em).stream()
                        .filter(p -> p.getQuantidadeTotal() > 0)
                        .collect(Collectors.toList())
        );
    }

    public List<Lote> buscarLotesDisponiveis(int produtoId) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findLotesByProdutoId(produtoId, em).stream()
                        .filter(l -> l.getQuantidade() > 0)
                        .collect(Collectors.toList())
        );
    }

    public Optional<Produto> buscarProdutoPorId(int id) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findById(id, em)
        );
    }

    public Optional<Lote> buscarLotePorId(int loteId) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findLoteById(loteId, em)
        );
    }

    public Produto salvarProduto(Produto produto, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.save(produto, ator, em)
        );
    }

    // MÉTODO ALTERADO PARA REGISTAR MOVIMENTOS
    public Lote salvarLote(Lote lote, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }

        return transactionService.executeInTransactionWithResult(em -> {
            // Guarda o lote para obter o ID e o estado mais recente
            Lote loteSalvo = produtoRepository.saveLote(lote, ator, em);

            // Cria um novo registo de movimento de estoque
            MovimentoEstoque movimento = new MovimentoEstoque();
            movimento.setProduto(loteSalvo.getProduto());
            movimento.setLote(loteSalvo);
            movimento.setQuantidade(loteSalvo.getQuantidade());
            movimento.setDataMovimento(LocalDateTime.now());
            movimento.setUsuario(ator);

            // Define o tipo de movimento
            boolean isUpdate = lote.getId() != 0;
            movimento.setTipoMovimento(isUpdate ? "AJUSTE" : "ENTRADA");

            // Persiste o novo movimento na base de dados
            em.persist(movimento);

            return loteSalvo;
        });
    }

    public Lote adicionarEstoqueLote(String nomeProduto, String numeroLote, int quantidadeAdicionar, Usuario ator) throws PersistenciaException, IllegalArgumentException {
        return transactionService.executeInTransactionWithResult(em -> {
            Produto produto = produtoRepository.findByNome(nomeProduto, em)
                    .orElseThrow(() -> new IllegalArgumentException("Produto '" + nomeProduto + "' não encontrado."));

            Lote lote = produto.getLotes().stream()
                    .filter(l -> l.getNumeroLote().equalsIgnoreCase(numeroLote))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Lote '" + numeroLote + "' não encontrado para o produto '" + nomeProduto + "'."));

            lote.setQuantidade(lote.getQuantidade() + quantidadeAdicionar);
            return produtoRepository.saveLote(lote, ator, em);
        });
    }

    public void removerLote(int loteId, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em ->
                produtoRepository.deleteLoteById(loteId, ator, em)
        );
    }

    public void alterarStatusProduto(int produtoId, boolean novoStatus, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em ->
                produtoRepository.updateStatusAtivo(produtoId, novoStatus, ator, em)
        );
    }
}