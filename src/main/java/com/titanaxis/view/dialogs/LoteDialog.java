// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/dialogs/LoteDialog.java
package com.titanaxis.view.dialogs;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.UIMessageUtil; // Importado

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;

public class LoteDialog extends JDialog {
    private final ProdutoService produtoService;
    private final Lote lote;
    private final Usuario ator;
    private final Produto produtoPai;
    private Lote loteSalvo;
    private JTextField numeroLoteField, quantidadeField;
    private JFormattedTextField dataValidadeField;

    public LoteDialog(Frame owner, ProdutoService ps, Produto produtoPai, Lote l, Usuario ator) {
        super(owner, "Detalhes do Lote", true);
        this.produtoService = ps;
        this.produtoPai = produtoPai;
        this.lote = (l != null) ? l : new Lote();
        this.ator = ator;

        setTitle(l == null || l.getId() == 0 ? "Novo Lote para " + produtoPai.getNome() : "Editar Lote");
        setLayout(new BorderLayout());
        initComponents();
        populateFields();
        pack();
        setLocationRelativeTo(owner);
    }

    public Optional<Lote> getLoteSalvo() {
        return Optional.ofNullable(loteSalvo);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        numeroLoteField = new JTextField(20);
        quantidadeField = new JTextField();
        try {
            MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
            dateFormatter.setPlaceholderCharacter('_');
            dataValidadeField = new JFormattedTextField(dateFormatter);
        } catch (ParseException e) {
            dataValidadeField = new JFormattedTextField();
        }

        formPanel.add(new JLabel("Número do Lote:"));
        formPanel.add(numeroLoteField);
        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(quantidadeField);
        formPanel.add(new JLabel("Data de Validade (dd/mm/aaaa):"));
        formPanel.add(dataValidadeField);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        if (lote.getId() != 0) {
            numeroLoteField.setText(lote.getNumeroLote());
            quantidadeField.setText(String.valueOf(lote.getQuantidade()));
            if (lote.getDataValidade() != null) {
                dataValidadeField.setText(lote.getDataValidade().format(DateTimeFormatter.ofPattern("ddMMyyyy")));
            }
        }
    }

    private void save() {
        try {
            lote.setNumeroLote(numeroLoteField.getText().trim());
            lote.setQuantidade(Integer.parseInt(quantidadeField.getText().trim()));
            String dataTexto = dataValidadeField.getText().replace("/", "").replace("_", "").trim();
            if (!dataTexto.isEmpty()) {
                lote.setDataValidade(LocalDate.parse(dataTexto, DateTimeFormatter.ofPattern("ddMMyyyy")));
            } else {
                lote.setDataValidade(null);
            }

            if (lote.getNumeroLote().isEmpty() || lote.getQuantidade() <= 0) {
                UIMessageUtil.showErrorMessage(this, "Número do lote e quantidade positiva são obrigatórios.", "Erro");
                return;
            }

            lote.setProduto(this.produtoPai);
            this.loteSalvo = produtoService.salvarLote(lote, ator);
            dispose();

        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, "A quantidade deve ser um número válido.", "Erro de Formato");
        } catch (java.time.format.DateTimeParseException e) {
            UIMessageUtil.showErrorMessage(this, "Data de validade inválida. Use o formato dd/mm/aaaa.", "Erro de Formato");
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), "Erro de Autenticação");
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro de Base de Dados ao salvar: " + e.getMessage(), "Erro de Persistência");
        } catch (Exception e) {
            AppLogger.getLogger().log(Level.SEVERE, "Erro inesperado ao salvar o lote.", e);
            UIMessageUtil.showErrorMessage(this, "Erro ao salvar o lote: " + e.getMessage(), "Erro Inesperado");
            this.loteSalvo = null;
        }
    }
}