package com.titanaxis.util.reporter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.titanaxis.util.I18n;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public abstract class AbstractPdfReportGenerator<T> {

    protected static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    protected static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
    protected static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    protected static final Font FONT_TOTAL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    // O Template Method principal
    public ByteArrayOutputStream generate(List<T> data) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(getPageSize());
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document);
        document.add(createTable(data));
        addFooter(document, data);

        document.close();
        return baos;
    }

    // Métodos que podem ser sobrescritos
    // ALTERAÇÃO: O tipo de retorno agora é a superclasse Rectangle, o que resolve o erro.
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }

    protected void addHeader(Document document) throws DocumentException {
        Paragraph title = new Paragraph(getReportTitle(), FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
    }

    protected void addFooter(Document document, List<T> data) throws DocumentException {
        getFooterText(data).ifPresent(footerText -> {
            Paragraph total = new Paragraph(footerText, FONT_TOTAL_BOLD);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(10);
            try {
                document.add(total);
            } catch (DocumentException e) {
                // Lidar com a exceção
            }
        });

        Paragraph date = new Paragraph(I18n.getString("report.pdf.generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))), FONT_BODY);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);
    }

    protected PdfPTable createTable(List<T> data) throws DocumentException {
        PdfPTable table = new PdfPTable(getTableHeaders().length);
        table.setWidthPercentage(100);
        table.setWidths(getColumnWidths());
        table.setSpacingBefore(10);

        addTableHeader(table);

        for (T item : data) {
            createTableRow(table, item);
        }
        return table;
    }

    protected void addTableHeader(PdfPTable table) {
        for (String headerTitle : getTableHeaders()) {
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

    protected PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }

    // Métodos Abstratos - Devem ser implementados pelas classes filhas
    protected abstract String getReportTitle();
    protected abstract String[] getTableHeaders();
    protected abstract float[] getColumnWidths() throws DocumentException;
    protected abstract void createTableRow(PdfPTable table, T item);
    protected abstract Optional<String> getFooterText(List<T> data);
}