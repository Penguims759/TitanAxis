// src/main/java/com/titanaxis/util/PdfReportGenerator.java
package com.titanaxis.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class PdfReportGenerator {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_TOTAL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public static ByteArrayOutputStream generateInventarioPdf(List<Produto> produtos) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "Relatório de Inventário");

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 2, 2, 2});
        table.setSpacingBefore(10);

        addTableHeader(table, "ID", "Nome", "Qtd. Total", "Preço Unit.", "Valor Total");

        double valorTotalInventario = 0.0;
        for (Produto produto : produtos) {
            double valorTotalItem = produto.getPreco() * produto.getQuantidadeTotal();
            valorTotalInventario += valorTotalItem;

            table.addCell(createCell(String.valueOf(produto.getId())));
            table.addCell(createCell(produto.getNome()));
            table.addCell(createCell(String.valueOf(produto.getQuantidadeTotal())));
            table.addCell(createCell(CURRENCY_FORMAT.format(produto.getPreco())));
            table.addCell(createCell(CURRENCY_FORMAT.format(valorTotalItem)));
        }

        document.add(table);
        addFooter(document, "Valor Total do Inventário: " + CURRENCY_FORMAT.format(valorTotalInventario));
        document.close();
        return baos;
    }

    public static ByteArrayOutputStream generateVendasPdf(List<Venda> vendas) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "Relatório de Vendas");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3, 3, 2});
        table.setSpacingBefore(10);

        addTableHeader(table, "ID Venda", "Data", "Cliente", "Valor Total");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        double valorTotalGeral = 0.0;
        for (Venda venda : vendas) {
            valorTotalGeral += venda.getValorTotal();
            table.addCell(createCell(String.valueOf(venda.getId())));
            table.addCell(createCell(venda.getDataVenda().format(formatter)));
            table.addCell(createCell(venda.getNomeCliente()));
            table.addCell(createCell(CURRENCY_FORMAT.format(venda.getValorTotal())));
        }

        document.add(table);
        addFooter(document, "Valor Total de Vendas: " + CURRENCY_FORMAT.format(valorTotalGeral));
        document.close();
        return baos;
    }

    // NOVO MÉTODO PARA GERAR PDFs DE AUDITORIA
    public static ByteArrayOutputStream generateAuditoriaPdf(String title, String[] headers, List<Vector<Object>> data) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Deitado para mais espaço
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, title);

        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        addTableHeader(table, headers);

        for(Vector<Object> rowData : data) {
            for(Object cellData : rowData) {
                table.addCell(createCell(cellData != null ? cellData.toString() : ""));
            }
        }

        document.add(table);

        Paragraph date = new Paragraph("Relatório gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), FONT_BODY);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingBefore(10);
        document.add(date);

        document.close();
        return baos;
    }

    private static void addHeader(Document document, String titleText) throws DocumentException {
        Paragraph title = new Paragraph(titleText, FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
    }

    private static void addFooter(Document document, String totalText) throws DocumentException {
        Paragraph total = new Paragraph(totalText, FONT_TOTAL_BOLD);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        document.add(total);

        Paragraph date = new Paragraph("Relatório gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), FONT_BODY);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(Color.DARK_GRAY);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase(headerTitle, FONT_HEADER));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.setPadding(5);
            table.addCell(header);
        }
    }

    private static PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }
}