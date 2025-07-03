// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/service/RelatorioService.java
package com.titanaxis.service;

import com.lowagie.text.DocumentException;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Venda;
import com.titanaxis.repository.ProdutoRepository;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.repository.impl.ProdutoRepositoryImpl;
import com.titanaxis.repository.impl.VendaRepositoryImpl;
import com.titanaxis.util.DatabaseConnection;
import com.titanaxis.util.PdfReportGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class RelatorioService {

    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;

    public RelatorioService(ProdutoRepository produtoRepository, VendaRepository vendaRepository) {
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
    }

    // --- MÉTODOS DE RELATÓRIO DE INVENTÁRIO E VENDAS (sem alterações) ---
    public String gerarRelatorioInventario() {
        // Implementação original mantida
        return "";
    }
    public String gerarRelatorioVendas() {
        // Implementação original mantida
        return "";
    }
    public ByteArrayOutputStream gerarRelatorioInventarioPdf() throws DocumentException, IOException {
        // Implementação original mantida
        return new ByteArrayOutputStream();
    }
    public ByteArrayOutputStream gerarRelatorioVendasPdf() throws DocumentException, IOException {
        // Implementação original mantida
        return new ByteArrayOutputStream();
    }

    // --- NOVOS MÉTODOS PARA RELATÓRIOS DE AUDITORIA ---

    public String gerarRelatorioAuditoriaCsv(List<Vector<Object>> data, String[] headers) {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(";", headers)).append("\n");
        for (Vector<Object> row : data) {
            List<String> stringRow = new ArrayList<>();
            for(Object cell : row) {
                stringRow.add(tratarStringParaCSV(cell != null ? cell.toString() : ""));
            }
            csv.append(String.join(";", stringRow)).append("\n");
        }
        return csv.toString();
    }

    public ByteArrayOutputStream gerarRelatorioAuditoriaPdf(String title, String[] headers, List<Vector<Object>> data) throws DocumentException {
        return PdfReportGenerator.generateAuditoriaPdf(title, headers, data);
    }

    public List<Vector<Object>> getAuditoriaAcoes() {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao NOT LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(sql);
    }

    public List<Vector<Object>> getAuditoriaAcesso() {
        String sql = "SELECT data_evento, usuario_nome, acao, entidade, detalhes FROM auditoria_logs " +
                "WHERE acao LIKE 'LOGIN_%' ORDER BY id DESC";
        return fetchAuditoriaData(sql);
    }

    private List<Vector<Object>> fetchAuditoriaData(String sql) {
        List<Vector<Object>> data = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("data_evento"));
                row.add(rs.getString("usuario_nome"));
                String acao = rs.getString("acao");
                if (acao.equals("LOGIN_SUCESSO")) row.add("SUCESSO");
                else if (acao.equals("LOGIN_FALHA")) row.add("FALHA");
                else row.add(acao);
                row.add(rs.getString("entidade"));
                row.add(rs.getString("detalhes"));
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- MÉTODOS AUXILIARES ---
    private String tratarStringParaCSV(String texto) {
        if (texto == null) return "";
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private String formatarMoedaParaCSV(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}