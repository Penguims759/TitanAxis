package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Lote;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.repository.ProdutoRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TransactionService transactionService;
    private final CategoriaRepository categoriaRepository;

    @Inject
    public ProdutoService(ProdutoRepository produtoRepository, TransactionService transactionService, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.transactionService = transactionService;
        this.categoriaRepository = categoriaRepository;
    }

    // MÉTODO CORRIGIDO: Adicionado try-catch para a IOException
    public String importarProdutosDeCsv(File ficheiro, Usuario ator) throws IOException, UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Apenas utilizadores autenticados podem importar produtos.");
        }

        final int[] sucessos = {0};
        final int[] falhas = {0};

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
            br.readLine();

            transactionService.executeInTransaction(em -> {
                try {
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        final String[] dados = linha.split(";", -1);
                        if (dados.length < 4) {
                            falhas[0]++;
                            continue;
                        }

                        try {
                            String nome = dados[0].trim();
                            String descricao = dados[1].trim();
                            double preco = Double.parseDouble(dados[2].trim().replace(",", "."));
                            String nomeCategoria = dados[3].trim();

                            Categoria categoria = categoriaRepository.findByNome(nomeCategoria, em)
                                    .orElse(categoriaRepository.findByNome("Geral", em)
                                            .orElseThrow(() -> new RuntimeException("A categoria padrão 'Geral' não foi encontrada.")));

                            Produto produto = new Produto(nome, descricao, preco, categoria);
                            produto.setAtivo(true);
                            produtoRepository.save(produto, ator, em);
                            sucessos[0]++;
                        } catch (Exception e) {
                            falhas[0]++;
                        }
                    }
                } catch (IOException e) {
                    // Embrulha a IOException numa RuntimeException para ser apanhada pelo TransactionService
                    throw new RuntimeException("Erro ao ler o ficheiro CSV dentro da transação.", e);
                }
            });
        }

        return String.format("Processo de importação concluído.\nSucessos: %d\nFalhas: %d", sucessos[0], falhas[0]);
    }

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

    public Lote salvarLote(Lote lote, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }

        return transactionService.executeInTransactionWithResult(em -> {
            Lote loteSalvo = produtoRepository.saveLote(lote, ator, em);

            MovimentoEstoque movimento = new MovimentoEstoque();
            movimento.setProduto(loteSalvo.getProduto());
            movimento.setLote(loteSalvo);
            movimento.setQuantidade(loteSalvo.getQuantidade());
            movimento.setDataMovimento(LocalDateTime.now());
            movimento.setUsuario(ator);

            boolean isUpdate = lote.getId() != 0;
            movimento.setTipoMovimento(isUpdate ? "AJUSTE" : "ENTRADA");

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