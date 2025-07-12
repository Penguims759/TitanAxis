package com.titanaxis.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;

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

        addHeader(document, I18n.getString("report.pdf.inventory.title")); // ALTERADO

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 2, 2, 2});
        table.setSpacingBefore(10);

        // ALTERADO
        addTableHeader(table,
                I18n.getString("report.inventory.header.id"),
                I18n.getString("report.inventory.header.name"),
                I18n.getString("report.inventory.header.totalQty"),
                I18n.getString("report.inventory.header.unitPrice"),
                I18n.getString("report.inventory.header.totalValue")
        );

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
        addFooter(document, I18n.getString("report.pdf.inventory.footer", CURRENCY_FORMAT.format(valorTotalInventario))); // ALTERADO
        document.close();
        return baos;
    }

    public static ByteArrayOutputStream generateVendasPdf(List<Venda> vendas) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, I18n.getString("report.pdf.sales.title")); // ALTERADO

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3, 3, 2});
        table.setSpacingBefore(10);

        // ALTERADO
        addTableHeader(table,
                I18n.getString("report.sales.header.id"),
                I18n.getString("report.sales.header.date"),
                I18n.getString("report.sales.header.client"),
                I18n.getString("report.sales.header.totalValue")
        );

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
        addFooter(document, I18n.getString("report.pdf.sales.footer", CURRENCY_FORMAT.format(valorTotalGeral))); // ALTERADO
        document.close();
        return baos;
    }

    public static ByteArrayOutputStream generateAuditoriaPdf(String title, String[] headers, List<Vector<Object>> data) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
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

        Paragraph date = new Paragraph(I18n.getString("report.pdf.generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))), FONT_BODY); // ALTERADO
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingBefore(10);
        document.add(date);

        document.close();
        return baos;
    }

    public static ByteArrayOutputStream generateReciboVendaPdf(Venda venda) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph title = new Paragraph(I18n.getString("receipt.pdf.title", venda.getId()), FONT_TITLE); // ALTERADO
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        // ALTERADO
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.client"), FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getCliente() != null ? venda.getCliente().getNome() : I18n.getString("general.notSpecified"), FONT_BODY));
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.date"), FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_BODY));
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.seller"), FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getUsuario().getNomeUsuario(), FONT_BODY));
        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{4, 2, 2, 2});
        // ALTERADO
        addTableHeader(itemsTable,
                I18n.getString("receipt.items.header.product"),
                I18n.getString("receipt.items.header.quantity"),
                I18n.getString("receipt.items.header.unitPrice"),
                I18n.getString("receipt.items.header.subtotal")
        );

        for (VendaItem item : venda.getItens()) {
            itemsTable.addCell(createCell(item.getProduto().getNome()));
            itemsTable.addCell(createCell(String.valueOf(item.getQuantidade())));
            itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getPrecoUnitario())));
            itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getQuantidade() * item.getPrecoUnitario())));
        }
        document.add(itemsTable);

        Paragraph total = new Paragraph(I18n.getString("receipt.pdf.totalValue", CURRENCY_FORMAT.format(venda.getValorTotal())), FONT_TITLE); // ALTERADO
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(20);
        document.add(total);

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

        Paragraph date = new Paragraph(I18n.getString("report.pdf.generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))), FONT_BODY); // ALTERADO
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

    private static PdfPCell createSimpleCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }
}