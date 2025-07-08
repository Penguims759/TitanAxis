package com.titanaxis.view.dialogs;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Devolucao;
import com.titanaxis.model.DevolucaoItem;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaItem;
import com.titanaxis.service.DevolucaoService;
import com.titanaxis.util.UIMessageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevolucaoDialog extends JDialog {

    private final DevolucaoService devolucaoService;
    private final Venda venda;
    private final Usuario ator;
    private final JTable itensTable;
    private final DefaultTableModel tableModel;
    private final JTextArea motivoArea;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public DevolucaoDialog(Frame owner, AppContext appContext, Venda venda) {
        super(owner, "Registrar Devolução da Venda #" + venda.getId(), true);
        this.devolucaoService = appContext.getDevolucaoService();
        this.venda = venda;
        this.ator = appContext.getAuthService().getUsuarioLogado().orElse(null);

        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        String[] columnNames = {"Produto", "Qtd. Vendida", "Preço Unit.", "Qtd. a Devolver"};
        tableModel = new DefaultTableModel(columnNames, 0);
        itensTable = new JTable(tableModel);
        motivoArea = new JTextArea(3, 30);

        initComponents();
        populateTable();
    }

    private void initComponents() {
        // Painel com a tabela de itens
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Itens da Venda"));
        tablePanel.add(new JScrollPane(itensTable), BorderLayout.CENTER);

        // Painel para o motivo
        JPanel motivoPanel = new JPanel(new BorderLayout(5,5));
        motivoPanel.setBorder(BorderFactory.createTitledBorder("Motivo da Devolução"));
        motivoPanel.add(new JScrollPane(motivoArea), BorderLayout.CENTER);

        // Painel inferior com botões
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Confirmar Devolução");
        saveButton.addActionListener(e -> registrarDevolucao());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        bottomPanel.add(motivoPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void populateTable() {
        for (VendaItem item : venda.getItens()) {
            tableModel.addRow(new Object[]{
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    currencyFormat.format(item.getSubtotal() / item.getQuantidade()),
                    0 // Inicia a quantidade a devolver como 0
            });
        }
    }

    private void registrarDevolucao() {
        Devolucao devolucao = new Devolucao();
        devolucao.setVenda(this.venda);
        devolucao.setMotivo(motivoArea.getText());

        List<DevolucaoItem> itensDevolvidos = new ArrayList<>();
        double valorTotalEstornado = 0.0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                int qtdDevolver = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                if (qtdDevolver > 0) {
                    VendaItem vendaItemOriginal = venda.getItens().get(i);
                    int qtdVendida = vendaItemOriginal.getQuantidade();

                    if (qtdDevolver > qtdVendida) {
                        UIMessageUtil.showErrorMessage(this, "A quantidade a devolver do produto '" + vendaItemOriginal.getProduto().getNome() + "' não pode ser maior que a quantidade vendida.", "Erro de Validação");
                        return;
                    }

                    DevolucaoItem itemDevolvido = new DevolucaoItem();
                    itemDevolvido.setVendaItem(vendaItemOriginal);
                    itemDevolvido.setQuantidadeDevolvida(qtdDevolver);
                    itemDevolvido.setDevolucao(devolucao);
                    itensDevolvidos.add(itemDevolvido);

                    double precoUnitario = vendaItemOriginal.getSubtotal() / vendaItemOriginal.getQuantidade();
                    valorTotalEstornado += precoUnitario * qtdDevolver;
                }
            } catch (NumberFormatException e) {
                UIMessageUtil.showErrorMessage(this, "A quantidade a devolver deve ser um número inteiro válido.", "Erro de Formato");
                return;
            }
        }

        if (itensDevolvidos.isEmpty()) {
            UIMessageUtil.showWarningMessage(this, "Nenhum item foi marcado para devolução.", "Aviso");
            return;
        }

        devolucao.setItens(itensDevolvidos);
        devolucao.setValorEstornado(valorTotalEstornado);

        try {
            devolucaoService.registrarDevolucao(devolucao, ator);
            UIMessageUtil.showInfoMessage(this, "Devolução registrada com sucesso!", "Sucesso");
            dispose();
        } catch (UtilizadorNaoAutenticadoException | PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao registrar devolução: " + e.getMessage(), "Erro Crítico");
        }
    }
}