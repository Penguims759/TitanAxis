package com.titanaxis.util.reporter;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.titanaxis.model.Produto;
import com.titanaxis.util.I18n;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InventarioPdfReportGenerator extends AbstractPdfReportGenerator<Produto> {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected String getReportTitle() {
        return I18n.getString("report.pdf.inventory.title");
    }

    @Override
    protected String[] getTableHeaders() {
        return new String[]{
                I18n.getString("report.inventory.header.id"),
                I18n.getString("report.inventory.header.name"),
                I18n.getString("report.inventory.header.totalQty"),
                I18n.getString("report.inventory.header.unitPrice"),
                I18n.getString("report.inventory.header.totalValue")
        };
    }

    @Override
    protected float[] getColumnWidths() throws DocumentException {
        return new float[]{1, 4, 2, 2, 2};
    }

    @Override
    protected void createTableRow(PdfPTable table, Produto produto) {
        double valorTotalItem = produto.getPreco() * produto.getQuantidadeTotal();
        table.addCell(createCell(String.valueOf(produto.getId())));
        table.addCell(createCell(produto.getNome()));
        table.addCell(createCell(String.valueOf(produto.getQuantidadeTotal())));
        table.addCell(createCell(CURRENCY_FORMAT.format(produto.getPreco())));
        table.addCell(createCell(CURRENCY_FORMAT.format(valorTotalItem)));
    }

    @Override
    protected Optional<String> getFooterText(List<Produto> data) {
        double valorTotalInventario = data.stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidadeTotal())
                .sum();
        return Optional.of(I18n.getString("report.pdf.inventory.footer", CURRENCY_FORMAT.format(valorTotalInventario)));
    }
}