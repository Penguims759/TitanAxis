package com.titanaxis.util.reporter;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.titanaxis.model.Venda;
import com.titanaxis.util.I18n;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class VendasPdfReportGenerator extends AbstractPdfReportGenerator<Venda> {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected String getReportTitle() {
        return I18n.getString("report.pdf.sales.title");
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{
                I18n.getString("report.sales.header.id"),
                I18n.getString("report.sales.header.date"),
                I18n.getString("report.sales.header.client"),
                I18n.getString("report.sales.header.totalValue")
        };
    }

    @Override
    protected float[] getColumnWidths() throws DocumentException {
        return new float[]{1.5f, 3, 3, 2};
    }

    @Override
    protected void createTableRow(PdfPTable table, Venda venda) {
        table.addCell(createCell(String.valueOf(venda.getId())));
        table.addCell(createCell(venda.getDataVenda().format(FORMATTER)));
        table.addCell(createCell(venda.getNomeCliente()));
        table.addCell(createCell(CURRENCY_FORMAT.format(venda.getValorTotal())));
    }

    @Override
    protected Optional<String> getFooterText(List<Venda> data) {
        double valorTotalGeral = data.stream().mapToDouble(Venda::getValorTotal).sum();
        return Optional.of(I18n.getString("report.pdf.sales.footer", CURRENCY_FORMAT.format(valorTotalGeral)));
    }
}