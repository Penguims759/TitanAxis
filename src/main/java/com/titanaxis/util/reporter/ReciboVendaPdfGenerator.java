package com.titanaxis.util.reporter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.util.I18n;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReciboVendaPdfGenerator {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ByteArrayOutputStream generate(Venda venda) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph title = new Paragraph(I18n.getString("receipt.pdf.title", venda.getId()), AbstractPdfReportGenerator.FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.client"), AbstractPdfReportGenerator.FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getCliente() != null ? venda.getCliente().getNome() : I18n.getString("general.notSpecified"), AbstractPdfReportGenerator.FONT_BODY));
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.date"), AbstractPdfReportGenerator.FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), AbstractPdfReportGenerator.FONT_BODY));
        infoTable.addCell(createSimpleCell(I18n.getString("receipt.header.seller"), AbstractPdfReportGenerator.FONT_TOTAL_BOLD));
        infoTable.addCell(createSimpleCell(venda.getUsuario().getNomeUsuario(), AbstractPdfReportGenerator.FONT_BODY));
        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{4, 2, 2, 2});
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

        Paragraph total = new Paragraph(I18n.getString("receipt.pdf.totalValue", CURRENCY_FORMAT.format(venda.getValorTotal())), AbstractPdfReportGenerator.FONT_TITLE);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(20);
        document.add(total);

        document.close();
        return baos;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(java.awt.Color.DARK_GRAY);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase(headerTitle, AbstractPdfReportGenerator.FONT_HEADER));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.setPadding(5);
            table.addCell(header);
        }
    }

    private PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, AbstractPdfReportGenerator.FONT_BODY));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createSimpleCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }
}