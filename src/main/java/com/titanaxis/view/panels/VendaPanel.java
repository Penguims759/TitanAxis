package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.*;
import com.titanaxis.service.*;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import com.titanaxis.view.renderer.LoteCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final JTable carrinhoTable;
    private final JComboBox<Cliente> clienteComboBox;
    private final JComboBox<Produto> produtoComboBox;
    private final JComboBox<Lote> loteComboBox;
    private final JSpinner quantidadeSpinner;
    private final JLabel totalLabel;
    private final JButton finalizarButton;
    private final JButton orcamentoButton;
    private final JButton usarCreditoButton;

    private final JComboBox<String> formaPagamentoComboBox;
    private final JSpinner parcelasSpinner;
    private final JLabel parcelasLabel;

    private Carrinho carrinho;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public VendaPanel(AppContext appContext) {
        this.appContext = appContext;
        this.authService = appContext.getAuthService();
        this.vendaService = appContext.getVendaService();
        this.clienteService = appContext.getClienteService();
        this.produtoService = appContext.getProdutoService();

        carrinhoTableModel = new DefaultTableModel(new String[]{
                I18n.getString("sale.cart.header.product"),
                I18n.getString("sale.cart.header.batch"),
                I18n.getString("sale.cart.header.quantity"),
                I18n.getString("sale.cart.header.unitPrice"),
                I18n.getString("sale.cart.header.discount"),
                I18n.getString("sale.cart.header.subtotal")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        carrinhoTable = new JTable(carrinhoTableModel);
        clienteComboBox = new JComboBox<>();
        produtoComboBox = new JComboBox<>();
        loteComboBox = new JComboBox<>();
        loteComboBox.setRenderer(new LoteCellRenderer());
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        totalLabel = new JLabel(I18n.getString("sale.label.total", currencyFormat.format(0.0)));
        finalizarButton = new JButton(I18n.getString("sale.button.finalize"));
        orcamentoButton = new JButton(I18n.getString("sale.button.saveQuote"));
        usarCreditoButton = new JButton(I18n.getString("sale.button.useCredit"));

        formaPagamentoComboBox = new JComboBox<>(new String[]{"À Vista", "Cartão de Crédito", "A Prazo"});
        parcelasLabel = new JLabel("Parcelas:");
        parcelasSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));


        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(carrinhoTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        addEventListeners();
        toggleParcelasVisibility();

        // ** CORREÇÃO APLICADA AQUI **
        // Garante que os dados sejam carregados na primeira vez que o painel é criado.
        refreshData();
    }

    @Override
    public void refreshData(){
        this.carrinho = new Carrinho(authService.getUsuarioLogado().orElse(null));
        carregarDadosIniciais();
        atualizarCarrinhoETotal();
    }

    private void addEventListeners() {
        carrinhoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = carrinhoTable.getSelectedRow();
                    if (selectedRow != -1) {
                        aplicarDescontoItem(selectedRow);
                    }
                }
            }
        });
        clienteComboBox.addActionListener(e -> atualizarEstadoBotaoCredito());

        formaPagamentoComboBox.addActionListener(e -> toggleParcelasVisibility());
    }

    private void toggleParcelasVisibility() {
        boolean isAPrazo = "A Prazo".equals(formaPagamentoComboBox.getSelectedItem());
        parcelasLabel.setVisible(isAPrazo);
        parcelasSpinner.setVisible(isAPrazo);
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
            UIMessageUtil.showErrorMessage(this, I18n.getString("error.loadInitialData", e.getMessage()), I18n.getString("error.db.title"));
        }
    }

    private void atualizarLotesDisponiveis() {
        loteComboBox.removeAllItems();
        Produto produtoSelecionado = (Produto) produtoComboBox.getSelectedItem();
        if (produtoSelecionado != null) {
            try {
                produtoService.buscarLotesDisponiveis(produtoSelecionado.getId()).forEach(loteComboBox::addItem);
            } catch (PersistenciaException e) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("error.loadBatches", e.getMessage()), I18n.getString("error.db.title"));
            }
        }
    }

    private void adicionarAoCarrinho() {
        try {
            carrinho.adicionarItem((Lote) loteComboBox.getSelectedItem(), (Integer) quantidadeSpinner.getValue());
            atualizarCarrinhoETotal();
        } catch (IllegalArgumentException e) {
            UIMessageUtil.showWarningMessage(this, e.getMessage(), I18n.getString("warning.title"));
        }
    }

    private void atualizarCarrinhoETotal() {
        carrinhoTableModel.setRowCount(0);
        for (VendaItem item : carrinho.getItens()) {
            double subtotal = item.getSubtotal();
            carrinhoTableModel.addRow(new Object[]{
                    item.getLote().getProduto().getNome(), item.getLote().getNumeroLote(),
                    item.getQuantidade(), currencyFormat.format(item.getPrecoUnitario()),
                    currencyFormat.format(item.getDesconto()),
                    currencyFormat.format(subtotal)
            });
        }
        totalLabel.setText(I18n.getString("sale.label.total", currencyFormat.format(carrinho.getValorTotal())));
        boolean hasItems = !carrinho.getItens().isEmpty();
        finalizarButton.setEnabled(hasItems);
        orcamentoButton.setEnabled(hasItems);
    }

    private void acaoLimparVenda() {
        try {
            if (!carrinho.getItens().isEmpty()) {
                vendaService.cancelarVenda(carrinho.getVenda(), authService.getUsuarioLogado().orElse(null));
            }
            refreshData();
        } catch (PersistenciaException | UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("error.cancelSale", e.getMessage()), I18n.getString("error.title"));
        }
    }

    private void prepararVenda() {
        carrinho.setCliente((Cliente) clienteComboBox.getSelectedItem());
        carrinho.getVenda().setFormaPagamento((String) formaPagamentoComboBox.getSelectedItem());
        if ("A Prazo".equals(carrinho.getVenda().getFormaPagamento())) {
            carrinho.getVenda().setNumeroParcelas((Integer) parcelasSpinner.getValue());
        } else {
            carrinho.getVenda().setNumeroParcelas(1);
        }
    }


    private void salvarOrcamento() {
        prepararVenda();
        try {
            Venda orcamentoSalvo = vendaService.salvarOrcamento(carrinho.getVenda(), authService.getUsuarioLogado().orElse(null));
            UIMessageUtil.showInfoMessage(this, I18n.getString("sale.quote.saveSuccess", orcamentoSalvo.getId()), I18n.getString("success.title"));
            refreshData();
        } catch (CarrinhoVazioException e) {
            UIMessageUtil.showWarningMessage(this, e.getMessage(), I18n.getString("warning.title"));
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.auth.title"));
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("error.db.generic", e.getMessage()), I18n.getString("error.persistence.title"));
        }
    }

    private void finalizarVenda() {
        prepararVenda();
        try {
            Venda vendaSalva = vendaService.finalizarVenda(carrinho.getVenda(), authService.getUsuarioLogado().orElse(null));
            UIMessageUtil.showInfoMessage(this, I18n.getString("sale.finalizeSuccess", vendaSalva.getId()), I18n.getString("success.title"));
            refreshData();
        } catch (CarrinhoVazioException e) {
            UIMessageUtil.showWarningMessage(this, e.getMessage(), I18n.getString("warning.title"));
        } catch (UtilizadorNaoAutenticadoException e) {
            UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.auth.title"));
        } catch (PersistenciaException e) {
            UIMessageUtil.showErrorMessage(this, I18n.getString("error.db.generic", e.getMessage()), I18n.getString("error.persistence.title"));
        }
    }

    private void aplicarDescontoItem(int rowIndex) {
        String descontoStr = JOptionPane.showInputDialog(this,
                I18n.getString("sale.dialog.discount.itemMessage"),
                I18n.getString("sale.dialog.discount.itemTitle"),
                JOptionPane.PLAIN_MESSAGE);
        if (descontoStr != null) {
            try {
                double desconto = Double.parseDouble(descontoStr.replace(",", "."));
                carrinho.aplicarDescontoItem(rowIndex, desconto);
                atualizarCarrinhoETotal();
            } catch (NumberFormatException e) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("error.format.invalidDiscount"), I18n.getString("error.format.title"));
            } catch (IllegalArgumentException e) {
                UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.validation.title"));
            }
        }
    }

    private void aplicarDescontoTotal() {
        String descontoStr = JOptionPane.showInputDialog(this,
                I18n.getString("sale.dialog.discount.totalMessage"),
                I18n.getString("sale.dialog.discount.totalTitle"),
                JOptionPane.PLAIN_MESSAGE);
        if (descontoStr != null) {
            try {
                double desconto = Double.parseDouble(descontoStr.replace(",", "."));
                carrinho.aplicarDescontoTotal(desconto);
                atualizarCarrinhoETotal();
            } catch (NumberFormatException e) {
                UIMessageUtil.showErrorMessage(this, I18n.getString("error.format.invalidDiscount"), I18n.getString("error.format.title"));
            } catch (IllegalArgumentException e) {
                UIMessageUtil.showErrorMessage(this, e.getMessage(), I18n.getString("error.validation.title"));
            }
        }
    }

    private void usarCreditoCliente() {
        Cliente cliente = (Cliente) clienteComboBox.getSelectedItem();
        if (cliente == null || cliente.getCredito() <= 0) return;

        double totalVenda = carrinho.getValorTotal();
        double creditoDisponivel = cliente.getCredito();
        double valorParaUsar = Math.min(totalVenda, creditoDisponivel);

        String msg = I18n.getString("sale.dialog.credit.message", cliente.getNome(), currencyFormat.format(creditoDisponivel), currencyFormat.format(valorParaUsar));

        if (UIMessageUtil.showConfirmDialog(this, msg, I18n.getString("sale.dialog.credit.title"))) {
            carrinho.aplicarCredito(valorParaUsar);
            atualizarCarrinhoETotal();
            usarCreditoButton.setEnabled(false);
        }
    }

    private void atualizarEstadoBotaoCredito() {
        Cliente cliente = (Cliente) clienteComboBox.getSelectedItem();
        if (cliente != null && cliente.getCredito() > 0) {
            usarCreditoButton.setEnabled(true);
            usarCreditoButton.setText(I18n.getString("sale.button.useCreditWithAmount", currencyFormat.format(cliente.getCredito())));
        } else {
            usarCreditoButton.setEnabled(false);
            usarCreditoButton.setText(I18n.getString("sale.button.useCredit"));
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("sale.border.newSale")));
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton addButton = new JButton(I18n.getString("sale.button.addToCart"));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; selectionPanel.add(new JLabel(I18n.getString("sale.label.client")), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; selectionPanel.add(clienteComboBox, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0; selectionPanel.add(new JLabel(I18n.getString("sale.label.product")), gbc);
        gbc.gridx = 1; gbc.weightx = 1; selectionPanel.add(produtoComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; selectionPanel.add(new JLabel(I18n.getString("sale.label.availableBatch")), gbc);
        gbc.gridx = 1; gbc.weightx = 1; selectionPanel.add(loteComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0; selectionPanel.add(new JLabel(I18n.getString("sale.label.quantity")), gbc);
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

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton descontoTotalButton = new JButton(I18n.getString("sale.button.applyTotalDiscount"));
        descontoTotalButton.addActionListener(e -> aplicarDescontoTotal());

        usarCreditoButton.addActionListener(e -> usarCreditoCliente());
        usarCreditoButton.setEnabled(false);

        leftPanel.add(usarCreditoButton);
        leftPanel.add(descontoTotalButton);

        // Painel de pagamento
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        paymentPanel.add(new JLabel("Pagamento:"));
        paymentPanel.add(formaPagamentoComboBox);
        paymentPanel.add(parcelasLabel);
        paymentPanel.add(parcelasSpinner);
        leftPanel.add(paymentPanel);


        leftPanel.add(totalLabel);

        bottomPanel.add(leftPanel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelarButton = new JButton(I18n.getString("sale.button.cancelSale"));
        cancelarButton.addActionListener(e -> acaoLimparVenda());

        orcamentoButton.addActionListener(e -> salvarOrcamento());
        finalizarButton.addActionListener(e -> finalizarVenda());

        buttonsPanel.add(cancelarButton);
        buttonsPanel.add(orcamentoButton);
        buttonsPanel.add(finalizarButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        return bottomPanel;
    }
}