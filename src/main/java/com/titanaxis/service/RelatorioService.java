// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/service/RelatorioService.java
package com.titanaxis.service;

import com.google.inject.Inject;
import com.lowagie.text.DocumentException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.util.PdfReportGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.stream.Collectors;

public class RelatorioService {
    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final TransactionService transactionService;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Inject
    public RelatorioService(ProdutoRepository produtoRepository, VendaRepository vendaRepository, AuditoriaRepository auditoriaRepository, TransactionService transactionService) {
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.transactionService = transactionService;
    }

    public String gerarRelatorioInventario() throws PersistenciaException {
        List<Produto> produtos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Nome;Categoria;Qtd. Total;Preço Unit.;Valor Total do Estoque\n");
        for (Produto p : produtos) {
            csv.append(p.getId()).append(";")
                    .append(tratarStringParaCSV(p.getNome())).append(";")
                    .append(tratarStringParaCSV(p.getNomeCategoria())).append(";")
                    .append(p.getQuantidadeTotal()).append(";")
                    .append(formatarMoedaParaCSV(p.getPreco())).append(";")
                    .append(formatarMoedaParaCSV(p.getPreco() * p.getQuantidadeTotal())).append("\n");
        }
        return csv.toString();
    }

    public ByteArrayOutputStream gerarRelatorioInventarioPdf() throws DocumentException, IOException, PersistenciaException {
        List<Produto> produtos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );
        return PdfReportGenerator.generateInventarioPdf(produtos);
    }

    public String gerarRelatorioVendas() throws PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findAll(em)
        );
        StringBuilder csv = new StringBuilder();
        csv.append("ID Venda;Data;Cliente;Utilizador;Valor Total\n");
        for (Venda v : vendas) {
            csv.append(v.getId()).append(";")
                    .append(v.getDataVenda().format(DATE_TIME_FORMATTER)).append(";")
                    .append(tratarStringParaCSV(v.getNomeCliente())).append(";")
                    .append(tratarStringParaCSV(v.getUsuario().getNomeUsuario())).append(";")
                    .append(formatarMoedaParaCSV(v.getValorTotal())).append("\n");
        }
        return csv.toString();
    }

    public ByteArrayOutputStream gerarRelatorioVendasPdf() throws DocumentException, IOException, PersistenciaException {
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findAll(em)
        );
        return PdfReportGenerator.generateVendasPdf(vendas);
    }

    public String gerarRelatorioAuditoriaCsv(List<Vector<Object>> data, String[] headers) {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(";", headers)).append("\n");
        for (Vector<Object> row : data) {
            List<String> stringRow = row.stream()
                    .map(cell -> tratarStringParaCSV(cell != null ? cell.toString() : ""))
                    .collect(Collectors.toList());
            csv.append(String.join(";", stringRow)).append("\n");
        }
        return csv.toString();
    }

    public ByteArrayOutputStream gerarRelatorioAuditoriaPdf(String title, String[] headers, List<Vector<Object>> data) throws DocumentException {
        return PdfReportGenerator.generateAuditoriaPdf(title, headers, data);
    }

    public List<Vector<Object>> getAuditoriaAcoes() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                auditoriaRepository.getAuditoriaAcoes(em)
        );
    }

    public List<Vector<Object>> getAuditoriaAcesso() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                auditoriaRepository.getAuditoriaAcesso(em)
        );
    }

    private String tratarStringParaCSV(String texto) {
        if (texto == null) return "";
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private String formatarMoedaParaCSV(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}