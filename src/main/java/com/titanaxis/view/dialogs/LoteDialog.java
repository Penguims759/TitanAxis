package com.titanaxis.view.dialogs;

import com.titanaxis.exception.LoteDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Lote;
import com.titanaxis.model.Produto;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LoteDialog extends JDialog {
    private final ProdutoService produtoService;
    private final Lote lote;
    private final Usuario ator;
    private final Produto produtoPai;
    private Lote loteSalvo;
    private final JTextField numeroLoteField;
    private final JTextField quantidadeField;
    private final JFormattedTextField dataValidadeField;

    public LoteDialog(Frame owner, ProdutoService ps, Produto produtoPai, Lote l, Usuario ator) {
        super(owner, I18n.getString("batchDialog.title"), true); 
        this.produtoService = ps;
        this.produtoPai = produtoPai;
        this.lote = (l != null) ? l : new Lote();
        this.ator = ator;

        
        setTitle(l == null || l.getId() == 0 ? I18n.getString("batchDialog.title.new", produtoPai.getNome()) : I18n.getString("batchDialog.title.edit"));
        setLayout(new BorderLayout());

        numeroLoteField = new JTextField(20);
        quantidadeField = new JTextField();

        JFormattedTextField tempValidadeField;
        try {
            MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
            dateFormatter.setPlaceholderCharacter('_');
            tempValidadeField = new JFormattedTextField(dateFormatter);
        } catch (ParseException e) {
            tempValidadeField = new JFormattedTextField();
            AppLogger.getLogger().error("Erro ao criar MaskFormatter para data de validade.", e);
        }
        this.dataValidadeField = tempValidadeField;

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

        
        formPanel.add(new JLabel(I18n.getString("batchDialog.label.number")));
        formPanel.add(numeroLoteField);
        formPanel.add(new JLabel(I18n.getString("batchDialog.label.quantity")));
        formPanel.add(quantidadeField);
        formPanel.add(new JLabel(I18n.getString("batchDialog.label.expiry")));
        formPanel.add(dataValidadeField);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(I18n.getString("button.save")); 
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton(I18n.getString("button.cancel")); 
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
                UIMessageUtil.showErrorMessage(this, I18n.getString("batchDialog.error.requiredFields"), I18n.getString("error.title")); 
                return;
            }

            lote.setProduto(this.produtoPai);

            if(lote.getId() == 0){
                this.loteSalvo = produtoService.registrarEntradaLote(lote, ator);
            } else {
                this.loteSalvo = produtoService.salvarLote(lote, ator);
            }

            dispose();

        } catch (NumberFormatException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("batchDialog.error.invalidQuantity"), I18n.getString("error.format.title")); 
        } catch (java.time.format.DateTimeParseException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("batchDialog.error.invalidDate"), I18n.getString("error.format.title")); 
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.auth.title")); 
        } catch (LoteDuplicadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.duplication.title")); 
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("batchDialog.error.save", e.getMessage()), I18n.getString("error.persistence.title")); 
        } catch (Exception e) {
            AppLogger.getLogger().error("Erro inesperado ao salvar o lote.", e);
            UIMessageUtil.showErrorMessage(this, I18n.getString("batchDialog.error.unexpectedSave", e.getMessage()), I18n.getString("error.unexpected.title")); 
            this.loteSalvo = null;
        }
    }
}