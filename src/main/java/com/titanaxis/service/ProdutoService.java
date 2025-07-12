// src/main/java/com/titanaxis/service/ProdutoService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.LoteDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Lote;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.util.I18n; // Importado

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    public boolean produtoExiste(String nome) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findByNome(nome, em).isPresent()
        );
    }

    public boolean loteExiste(String nomeProduto, String numeroLote) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            Optional<Produto> produtoOpt = produtoRepository.findByNome(nomeProduto, em);
            if (produtoOpt.isPresent()) {
                return produtoOpt.get().getLotes().stream()
                        .anyMatch(lote -> lote.getNumeroLote().equalsIgnoreCase(numeroLote));
            }
            return false;
        });
    }


    public String importarProdutosDeCsv(File ficheiro, Usuario ator) throws IOException, UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.product.error.importAuth")); // ALTERADO
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
                                            .orElseThrow(() -> new RuntimeException(I18n.getString("service.product.error.defaultCategoryNotFound")))); // ALTERADO

                            Produto produto = new Produto(nome, descricao, preco, categoria);
                            produto.setAtivo(true);
                            produtoRepository.save(produto, ator, em);
                            sucessos[0]++;
                        } catch (Exception e) {
                            falhas[0]++;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(I18n.getString("service.product.error.csvRead"), e); // ALTERADO
                }
            });
        }

        return I18n.getString("service.product.importResult", sucessos[0], falhas[0]); // ALTERADO
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
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        return transactionService.executeInTransactionWithResult(em ->
                produtoRepository.save(produto, ator, em)
        );
    }

    public Lote salvarLote(Lote lote, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException, LoteDuplicadoException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        try {
            return transactionService.executeInTransactionWithResult(em -> {
                if (lote.getId() == 0) {
                    boolean loteExiste = lote.getProduto().getLotes().stream()
                            .anyMatch(l -> l.getNumeroLote().equalsIgnoreCase(lote.getNumeroLote()));
                    if (loteExiste) {
                        throw new RuntimeException(I18n.getString("service.product.error.batchExists", lote.getNumeroLote())); // ALTERADO
                    }
                }
                return produtoRepository.saveLote(lote, ator, em);
            });
        } catch (RuntimeException e) {
            if (e.getMessage().contains(I18n.getString("service.product.error.batchExists.check"))) { // ALTERADO
                throw new LoteDuplicadoException(e.getMessage());
            }
            throw e;
        }
    }

    public Lote registrarEntradaLote(Lote lote, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException, LoteDuplicadoException {
        Lote loteSalvo = salvarLote(lote, ator);
        transactionService.executeInTransaction(em -> {
            MovimentoEstoque movimento = new MovimentoEstoque();
            movimento.setProduto(loteSalvo.getProduto());
            movimento.setLote(loteSalvo);
            movimento.setQuantidade(loteSalvo.getQuantidade());
            movimento.setDataMovimento(LocalDateTime.now());
            movimento.setUsuario(ator);
            movimento.setTipoMovimento("ENTRADA");
            em.persist(movimento);
        });
        return loteSalvo;
    }

    public Lote ajustarEstoqueLote(String nomeProduto, String numeroLote, int novaQuantidade, Usuario ator) throws PersistenciaException, IllegalArgumentException {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException(I18n.getString("service.product.error.negativeQuantity")); // ALTERADO
        }
        return transactionService.executeInTransactionWithResult(em -> {
            Produto produto = produtoRepository.findByNome(nomeProduto, em)
                    .orElseThrow(() -> new IllegalArgumentException(I18n.getString("service.product.error.notFound", nomeProduto))); // ALTERADO

            Lote lote = produto.getLotes().stream()
                    .filter(l -> l.getNumeroLote().equalsIgnoreCase(numeroLote))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(I18n.getString("service.product.error.batchNotFoundForProduct", numeroLote, nomeProduto))); // ALTERADO

            int quantidadeAnterior = lote.getQuantidade();
            int diferenca = novaQuantidade - quantidadeAnterior;

            if (diferenca != 0) {
                lote.setQuantidade(novaQuantidade);
                Lote loteSalvo = produtoRepository.saveLote(lote, ator, em);

                MovimentoEstoque movimento = new MovimentoEstoque();
                movimento.setProduto(lote.getProduto());
                movimento.setLote(lote);
                movimento.setQuantidade(Math.abs(diferenca));
                movimento.setDataMovimento(LocalDateTime.now());
                movimento.setUsuario(ator);
                movimento.setTipoMovimento("AJUSTE");
                em.persist(movimento);
                return loteSalvo;
            }
            return lote;
        });
    }

    public Lote adicionarEstoqueLote(String nomeProduto, String numeroLote, int quantidadeAdicionar, Usuario ator) throws PersistenciaException, IllegalArgumentException {
        return transactionService.executeInTransactionWithResult(em -> {
            Produto produto = produtoRepository.findByNome(nomeProduto, em)
                    .orElseThrow(() -> new IllegalArgumentException(I18n.getString("service.product.error.notFound", nomeProduto))); // Reaproveitado

            Lote lote = produto.getLotes().stream()
                    .filter(l -> l.getNumeroLote().equalsIgnoreCase(numeroLote))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(I18n.getString("service.product.error.batchNotFoundForProduct", numeroLote, nomeProduto))); // Reaproveitado

            lote.setQuantidade(lote.getQuantidade() + quantidadeAdicionar);
            Lote loteSalvo = produtoRepository.saveLote(lote, ator, em);

            MovimentoEstoque movimento = new MovimentoEstoque();
            movimento.setProduto(lote.getProduto());
            movimento.setLote(lote);
            movimento.setQuantidade(quantidadeAdicionar);
            movimento.setDataMovimento(LocalDateTime.now());
            movimento.setUsuario(ator);
            movimento.setTipoMovimento("ENTRADA");
            em.persist(movimento);
            return loteSalvo;
        });
    }

    public void removerLote(int loteId, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        transactionService.executeInTransaction(em ->
                produtoRepository.deleteLoteById(loteId, ator, em)
        );
    }

    public void alterarStatusProduto(int produtoId, boolean novoStatus, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }
        transactionService.executeInTransaction(em ->
                produtoRepository.updateStatusAtivo(produtoId, novoStatus, ator, em)
        );
    }

    public String ajustarEstoquePercentual(String nomeProduto, double percentual, Usuario ator) throws PersistenciaException, IllegalArgumentException {
        if (ator == null) {
            throw new IllegalArgumentException(I18n.getString("service.auth.error.notAuthenticated")); // Reaproveitado
        }

        return transactionService.executeInTransactionWithResult(em -> {
            Produto produto = produtoRepository.findByNome(nomeProduto, em)
                    .orElseThrow(() -> new IllegalArgumentException(I18n.getString("service.product.error.notFound", nomeProduto))); // Reaproveitado

            int quantidadeAtual = produto.getQuantidadeTotal();
            if (quantidadeAtual == 0 && percentual > 0) {
                throw new IllegalArgumentException(I18n.getString("service.product.error.adjustZeroStock")); // ALTERADO
            }

            int quantidadeAjuste = (int) Math.round(quantidadeAtual * (percentual / 100.0));
            int novaQuantidadeTotal = quantidadeAtual + quantidadeAjuste;

            if (novaQuantidadeTotal < 0) {
                throw new IllegalArgumentException(I18n.getString("service.product.error.adjustResultsInNegative")); // ALTERADO
            }

            Lote loteParaAjuste = produto.getLotes().stream()
                    .filter(l -> l.getQuantidade() > 0)
                    .max(Comparator.comparing(Lote::getId))
                    .orElse(produto.getLotes().stream().findFirst().orElse(null));

            if (loteParaAjuste == null) {
                throw new IllegalArgumentException(I18n.getString("service.product.error.noBatchesToAdjust")); // ALTERADO
            }

            int novaQuantidadeLote = loteParaAjuste.getQuantidade() + quantidadeAjuste;
            if (novaQuantidadeLote < 0) {
                throw new IllegalArgumentException(I18n.getString("service.product.error.batchGoesNegative")); // ALTERADO
            }
            loteParaAjuste.setQuantidade(novaQuantidadeLote);
            produtoRepository.saveLote(loteParaAjuste, ator, em);

            MovimentoEstoque movimento = new MovimentoEstoque();
            movimento.setProduto(produto);
            movimento.setLote(loteParaAjuste);
            movimento.setQuantidade(Math.abs(quantidadeAjuste));
            movimento.setDataMovimento(LocalDateTime.now());
            movimento.setUsuario(ator);
            movimento.setTipoMovimento(quantidadeAjuste >= 0 ? "AJUSTE_AUMENTO" : "AJUSTE_REDUCAO");
            em.persist(movimento);

            return I18n.getString("service.product.adjustSuccess", nomeProduto, quantidadeAjuste, percentual, quantidadeAtual, novaQuantidadeTotal); // ALTERADO
        });
    }
}