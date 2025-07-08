package com.titanaxis.service;

import com.google.inject.Inject;
import com.lowagie.text.DocumentException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem; // Importação necessária
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
        // ... (código existente)
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
        // ... (código existente)
        List<Produto> produtos = transactionService.executeInTransactionWithResult(em ->
                produtoRepository.findAllIncludingInactive(em)
        );
        return PdfReportGenerator.generateInventarioPdf(produtos);
    }

    public String gerarRelatorioVendas() throws PersistenciaException {
        // ... (código existente)
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
        // ... (código existente)
        List<Venda> vendas = transactionService.executeInTransactionWithResult(em ->
                vendaRepository.findAll(em)
        );
        return PdfReportGenerator.generateVendasPdf(vendas);
    }

    public ByteArrayOutputStream gerarReciboVendaPdf(Venda venda) throws DocumentException {
        return PdfReportGenerator.generateReciboVendaPdf(venda);
    }

    // NOVO MÉTODO
    public String gerarReciboVendaCsv(Venda venda) {
        StringBuilder csv = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Cabeçalho do Recibo
        csv.append("Recibo da Venda #").append(venda.getId()).append("\n");
        csv.append("Data;").append(venda.getDataVenda().format(formatter)).append("\n");
        csv.append("Cliente;").append(tratarStringParaCSV(venda.getCliente() != null ? venda.getCliente().getNome() : "N/A")).append("\n");
        csv.append("Vendedor;").append(tratarStringParaCSV(venda.getUsuario() != null ? venda.getUsuario().getNomeUsuario() : "N/A")).append("\n\n");

        // Itens
        csv.append("Produto;Lote;Quantidade;Preco Unitario;Subtotal\n");
        for (VendaItem item : venda.getItens()) {
            csv.append(tratarStringParaCSV(item.getProduto().getNome())).append(";")
                    .append(tratarStringParaCSV(item.getLote().getNumeroLote())).append(";")
                    .append(item.getQuantidade()).append(";")
                    .append(formatarMoedaParaCSV(item.getPrecoUnitario())).append(";")
                    .append(formatarMoedaParaCSV(item.getQuantidade() * item.getPrecoUnitario())).append("\n");
        }

        // Total
        csv.append("\n;;;;Total\n");
        csv.append(";;;;").append(formatarMoedaParaCSV(venda.getValorTotal())).append("\n");

        return csv.toString();
    }

    public String gerarRelatorioAuditoriaCsv(List<Vector<Object>> data, String[] headers) {
        // ... (código existente)
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