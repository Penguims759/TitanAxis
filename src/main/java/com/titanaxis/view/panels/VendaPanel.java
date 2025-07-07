package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.CarrinhoVazioException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.*;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.service.VendaService;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.util.AppLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VendaPanel extends JPanel {

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

    private final List<VendaItem> carrinho = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final Logger logger = AppLogger.getLogger();

    public VendaPanel(AppContext appContext) {
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

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        JTable carrinhoTable = new JTable(carrinhoTableModel);
        add(new JScrollPane(carrinhoTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        carregarDadosIniciais();
    }

    /**
     * CORRIGIDO: Adicionado um null-check para evitar NullPointerException.
     * Permite que um cliente seja selecionado programaticamente na ComboBox.
     *
     * @param clienteParaSelecionar O cliente que deve ser selecionado.
     */
    public void selecionarCliente(Cliente clienteParaSelecionar) {
        if (clienteParaSelecionar == null) {
            logger.warning("Tentativa de selecionar um cliente nulo no painel de Vendas.");
            return;
        }
        for (int i = 0; i < clienteComboBox.getItemCount(); i++) {
            Cliente clienteNaLista = clienteComboBox.getItemAt(i);
            if (clienteNaLista != null && clienteNaLista.getId() == clienteParaSelecionar.getId()) {
                clienteComboBox.setSelectedIndex(i);
                return;
            }
        }
        logger.warning("Cliente com ID " + clienteParaSelecionar.getId() + " não encontrado na ComboBox de vendas.");
    }

    private void carregarDadosIniciais() {
        try {
            Cliente clienteSelecionadoAntes = (Cliente) clienteComboBox.getSelectedItem();
            clienteComboBox.removeAllItems();
            clienteComboBox.addItem(null);
            List<Cliente> clientes = clienteService.listarTodos();
            clientes.forEach(clienteComboBox::addItem);

            if (clienteSelecionadoAntes != null) {
                for (Cliente cliente : clientes) {
                    if (cliente.getId() == clienteSelecionadoAntes.getId()) {
                        clienteComboBox.setSelectedItem(cliente);
                        break;
                    }
                }
            }

            produtoComboBox.removeAllItems();
            produtoService.listarProdutosAtivosParaVenda().forEach(produtoComboBox::addItem);

            atualizarLotesDisponiveis();
        } catch (PersistenciaException e) {
            logger.log(Level.SEVERE, "Erro ao carregar dados iniciais de venda.", e);
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
                logger.log(Level.SEVERE, "Erro ao carregar lotes para o produto " + produtoSelecionado.getNome(), e);
                UIMessageUtil.showErrorMessage(this, "Erro ao carregar lotes: " + e.getMessage(), "Erro de Base de Dados");
            }
        }
    }

    private void adicionarAoCarrinho() {
        logger.info("Tentando adicionar item ao carrinho.");
        Lote loteSelecionado = (Lote) loteComboBox.getSelectedItem();
        int quantidade = (Integer) quantidadeSpinner.getValue();

        logger.info("Lote selecionado: " + (loteSelecionado != null ? loteSelecionado.getNumeroLote() : "null") + ", Quantidade: " + quantidade);

        if (loteSelecionado == null) {
            UIMessageUtil.showWarningMessage(this, "Selecione um produto e um lote válido.", "Aviso");
            logger.warning("Falha ao adicionar item ao carrinho: Nenhum lote selecionado.");
            return;
        }

        if (quantidade <= 0) {
            UIMessageUtil.showErrorMessage(this, "A quantidade deve ser maior que zero.", "Erro de Quantidade");
            logger.warning("Falha ao adicionar item ao carrinho: Quantidade é zero ou menor.");
            return;
        }

        if (quantidade > loteSelecionado.getQuantidade()) {
            UIMessageUtil.showErrorMessage(this, "Quantidade solicitada excede o estoque do lote (" + loteSelecionado.getQuantidade() + ").", "Erro de Estoque");
            logger.warning("Falha ao adicionar item ao carrinho: Estoque insuficiente.");
            return;
        }

        VendaItem novoItem = new VendaItem(loteSelecionado, quantidade);
        carrinho.add(novoItem);
        atualizarCarrinho();
        UIMessageUtil.showInfoMessage(this, "Item adicionado ao carrinho: " + loteSelecionado.getProduto().getNome() + " x" + quantidade, "Sucesso");
        logger.info("Item adicionado com sucesso ao carrinho.");
    }

    private void atualizarCarrinho() {
        carrinhoTableModel.setRowCount(0);
        double total = 0.0;
        for (VendaItem item : carrinho) {
            double subtotal = item.getPrecoUnitario() * item.getQuantidade();
            carrinhoTableModel.addRow(new Object[]{
                    item.getLote().getProduto().getNome(),
                    item.getLote().getNumeroLote(),
                    item.getQuantidade(),
                    currencyFormat.format(item.getPrecoUnitario()),
                    currencyFormat.format(subtotal)
            });
            total += subtotal;
        }
        totalLabel.setText("Total: " + currencyFormat.format(total));
        quantidadeSpinner.setValue(1);
    }

    private void limparVenda() {
        carrinho.clear();
        atualizarCarrinho();
        if (clienteComboBox.getItemCount() > 0) clienteComboBox.setSelectedIndex(0);
        carregarDadosIniciais();
    }

    private void finalizarVenda() {
        Cliente clienteSelecionado = (Cliente) clienteComboBox.getSelectedItem();
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        double valorTotal = carrinho.stream().mapToDouble(item -> item.getQuantidade() * item.getPrecoUnitario()).sum();
        Venda novaVenda = new Venda(clienteSelecionado, ator, valorTotal);
        carrinho.forEach(novaVenda::adicionarItem);

        try {
            vendaService.finalizarVenda(novaVenda, ator);
            UIMessageUtil.showInfoMessage(this, "Venda finalizada com sucesso!", "Sucesso");
            limparVenda();
        } catch (CarrinhoVazioException e) {
            logger.log(Level.WARNING, "Tentativa de finalizar venda com carrinho vazio.", e);
            UIMessageUtil.showWarningMessage(this, e.getMessage(), "Aviso");
        } catch (UtilizadorNaoAutenticadoException e) {
            logger.log(Level.WARNING, "Tentativa de finalizar venda sem utilizador autenticado.", e);
            UIMessageUtil.showErrorMessage(this, e.getMessage(), "Erro de Autenticação");
        } catch (PersistenciaException e) {
            logger.log(Level.SEVERE, "Erro de base de dados ao finalizar a venda. Causa: " + e.getMessage(), e);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro de base de dados ao finalizar a venda:\n" + e.getMessage(), "Erro de Persistência");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro inesperado ao finalizar a venda.", e);
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro inesperado ao finalizar a venda. Consulte os logs para mais detalhes.", "Erro");
        }
    }

    public void refreshData() {
        carregarDadosIniciais();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nova Venda"));
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton addButton = new JButton("Adicionar ao Carrinho");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        selectionPanel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        selectionPanel.add(clienteComboBox, gbc);
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        selectionPanel.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1;
        selectionPanel.add(produtoComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        selectionPanel.add(new JLabel("Lote Disponível:"), gbc);
        gbc.gridx = 1;
        selectionPanel.add(loteComboBox, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
        selectionPanel.add(new JLabel("Qtd:"), gbc);
        gbc.gridx = 3;
        selectionPanel.add(quantidadeSpinner, gbc);
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
        cancelarButton.addActionListener(e -> limparVenda());
        JButton finalizarButton = new JButton("Finalizar Venda");
        finalizarButton.addActionListener(e -> finalizarVenda());
        buttonsPanel.add(cancelarButton);
        buttonsPanel.add(finalizarButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        return bottomPanel;
    }
}