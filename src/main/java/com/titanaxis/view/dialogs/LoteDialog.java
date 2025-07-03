// FICHEIRO NOVO: src/main/java/com/titanaxis/view/dialogs/LoteDialog.java
package com.titanaxis.view.dialogs;

import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.ProdutoService;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoteDialog extends JDialog {
    private final ProdutoService produtoService;
    private final Lote lote;
    private final Usuario ator;
    private boolean saved = false;

    private JTextField numeroLoteField, quantidadeField;
    private JFormattedTextField dataValidadeField;

    public LoteDialog(Frame owner, ProdutoService ps, Produto produtoPai, Lote l, Usuario ator) {
        super(owner, "Detalhes do Lote", true);
        this.produtoService = ps;
        this.lote = (l != null) ? l : new Lote(produtoPai.getId(), "", 0, null);
        this.ator = ator;

        setTitle(l == null || l.getId() == 0 ? "Novo Lote para " + produtoPai.getNome() : "Editar Lote");
        setLayout(new BorderLayout());

        initComponents();
        populateFields();

        pack();
        setLocationRelativeTo(owner);
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

            if(lote.getNumeroLote().isEmpty() || lote.getQuantidade() <= 0) {
                JOptionPane.showMessageDialog(this, "Número do lote e quantidade positiva são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            produtoService.salvarLote(lote, ator);
            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "A quantidade deve ser um número válido.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Data de validade inválida. Use o formato dd/mm/aaaa.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o lote: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}