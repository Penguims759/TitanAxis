package com.titanaxis.util.reporter;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle; // IMPORT ADICIONADO
import com.lowagie.text.pdf.PdfPTable;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class AuditoriaPdfReportGenerator extends AbstractPdfReportGenerator<Vector<Object>> {

    private final String title;
    private final String[] headers;

    public AuditoriaPdfReportGenerator(String title, String[] headers) {
        this.title = title;
        this.headers = headers;
    }

    @Override
    protected Rectangle getPageSize() { // TIPO DE RETORNO CORRIGIDO
        return PageSize.A4.rotate();
    }

    @Override
    protected String getReportTitle() {
        return this.title;
    }

    @Override
    protected String[] getTableHeaders() {
        return this.headers;
    }

    @Override
    protected float[] getColumnWidths() throws DocumentException {
        // Largura igual para todas as colunas
        float[] widths = new float[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = 1f;
        }
        return widths;
    }

    @Override
    protected void createTableRow(PdfPTable table, Vector<Object> rowData) {
        for (Object cellData : rowData) {
            table.addCell(createCell(cellData != null ? cellData.toString() : ""));
        }
    }

    @Override
    protected Optional<String> getFooterText(List<Vector<Object>> data) {
        return Optional.empty(); // Relatório de auditoria não tem um total
    }
}