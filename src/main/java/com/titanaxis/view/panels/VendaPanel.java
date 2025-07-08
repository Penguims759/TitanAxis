// src/main/java/com/titanaxis/view/panels/VendaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.*;
import com.titanaxis.service.*;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class VendaPanel extends JPanel implements DashboardFrame.Refreshable {

    private final AppContext appContext;
    private final VendaService vendaService;
    private final AuthService authService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    private final DefaultTableModel carrinhoTableModel;
    private final JComboBox<Cliente> clienteComboBox;
    private final JComboBox<Produto> produtoComboBox;
    private final JComboBox<Lote> loteComboBox;
    private final JSpinner quantidadeSpinner;
    private final JLabel totalLabel;
    private final JButton finalizarButton;

    private Carrinho carrinho;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public VendaPanel(AppContext appContext) {
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.vendaService = appContext.getVendaService();
        this.clienteService = appContext.getClienteService();
        this.produtoService = appContext.getProdutoService();

        carrinhoTableModel = new DefaultTableModel(new String[]{"Produto", "Lote", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        clienteComboBox = new JComboBox<>();
        produtoComboBox = new JComboBox<>();
        loteComboBox = new JComboBox<>();
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        totalLabel = new JLabel("Total: " + currencyFormat.format(0.0));
        finalizarButton = new JButton("Finalizar Venda");

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        JTable carrinhoTable = new JTable(carrinhoTableModel);
        add(new JScrollPane(carrinhoTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        this.carrinho = new Carrinho(authService.getUsuarioLogado().orElse(null));
        carregarDadosIniciais();
        atualizarCarrinhoETotal();
    }

    public void selecionarCliente(Cliente clienteParaSelecionar) {
        if(clienteParaSelecionar != null) clienteComboBox.setSelectedItem(clienteParaSelecionar);
    }

    private void carregarDadosIniciais() {
        try {
            Cliente clienteSelecionadoAntes = (Cliente) clienteComboBox.getSelectedItem();
            clienteComboBox.removeAllItems();
            clienteComboBox.addItem(null);
            List<Cliente> clientes = clienteService.listarTodos();
            clientes.forEach(clienteComboBox::addItem);
            if (clienteSelecionadoAntes != null) clienteComboBox.setSelectedItem(clienteSelecionadoAntes);

            produtoComboBox.removeAllItems();
            produtoService.listarProdutosAtivosParaVenda().forEach(produtoComboBox::addItem);

            atualizarLotesDisponiveis();

        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro de Base de Dados");
        }
    }

    private void atualizarLotesDisponiveis() {
        loteComboBox.removeAllItems();
        Produto produtoSelecionado = (Produto) produtoComboBox.getSelectedItem();
        if (produtoSelecionado != null) {
            try {
                produtoService.buscarLotesDisponiveis(produtoSelecionado.getId()).forEach(loteComboBox::addItem);
            } catch (PersistenciaException e) {
                UIMessageUtil.showErrorMessage(this, "Erro ao carregar lotes: " + e.getMessage(), "Erro de Base de Dados");
            }
        }
    }

    private void adicionarAoCarrinho() {
        try {
            carrinho.adicionarItem((Lote) loteComboBox.getSelectedItem(), (Integer) quantidadeSpinner.getValue());
            atualizarCarrinhoETotal();
        } catch (IllegalArgumentException e) {
            UIMessageUtil.showWarningMessage(this, e.getMessage(), "Aviso");
        }
    }

    private void atualizarCarrinhoETotal() {
        carrinhoTableModel.setRowCount(0);
        for (VendaItem item : carrinho.getItens()) {
            double subtotal = item.getPrecoUnitario() * item.getQuantidade();
            carrinhoTableModel.addRow(new Object[]{
                    item.getLote().getProduto().getNome(), item.getLote().getNumeroLote(),
                    item.getQuantidade(), currencyFormat.format(item.getPrecoUnitario()),
                    currencyFormat.format(subtotal)
            });
        }
        totalLabel.setText("Total: " + currencyFormat.format(carrinho.getValorTotal()));
        finalizarButton.setEnabled(!carrinho.getItens().isEmpty());
    }

    private void acaoLimparVenda() {
        try {
            // Apenas cancela se a venda tiver itens. Se estiver vazia, apenas limpa a tela.
            if (!carrinho.getItens().isEmpty()) {
                vendaService.cancelarVenda(carrinho.getVenda(), authService.getUsuarioLogado().orElse(null));
            }
            refreshData(); // Cria um novo carrinho e reinicia a tela
        } catch (PersistenciaException | UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, "Erro ao cancelar a venda: " + e.getMessage(), "Erro");
        }
    }

    private void finalizarVenda() {
        carrinho.setCliente((Cliente) clienteComboBox.getSelectedItem());
        try {
            Venda vendaSalva = vendaService.finalizarVenda(carrinho.getVenda(), authService.getUsuarioLogado().orElse(null));
            UIMessageUtil.showInfoMessage(this, "Venda #" + vendaSalva.getId() + " finalizada com sucesso!", "Sucesso");
            refreshData();
        } catch (CarrinhoVazioException e) {
            UIMessageUtil.showWarningMessage(this, e.getMessage(), "Aviso");
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), "Erro de Autenticação");
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro de base de dados: " + e.getMessage(), "Erro de Persistência");
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nova Venda"));
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton addButton = new JButton("Adicionar ao Carrinho");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; selectionPanel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; selectionPanel.add(clienteComboBox, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0; selectionPanel.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; selectionPanel.add(produtoComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; selectionPanel.add(new JLabel("Lote Disponível:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; selectionPanel.add(loteComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0; selectionPanel.add(new JLabel("Qtd:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.2; selectionPanel.add(quantidadeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        addButton.addActionListener(e -> adicionarAoCarrinho());
        selectionPanel.add(addButton, gbc);

        produtoComboBox.addActionListener(e -> atualizarLotesDisponiveis());
        topPanel.add(selectionPanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelarButton = new JButton("Cancelar Venda");
        cancelarButton.addActionListener(e -> acaoLimparVenda());
        finalizarButton.addActionListener(e -> finalizarVenda());

        buttonsPanel.add(cancelarButton);
        buttonsPanel.add(finalizarButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        return bottomPanel;
    }
}