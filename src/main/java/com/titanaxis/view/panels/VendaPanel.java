package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.*;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.service.VendaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaPanel extends JPanel {

    private final VendaService vendaService;
    private final AuthService authService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    private DefaultTableModel carrinhoTableModel;
    private JComboBox<Cliente> clienteComboBox;
    private JComboBox<Produto> produtoComboBox;
    private JComboBox<Lote> loteComboBox;
    private JSpinner quantidadeSpinner;
    private JLabel totalLabel;

    // A lista de itens do carrinho agora é do tipo da entidade JPA
    private final List<VendaItem> carrinho = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public VendaPanel(AppContext appContext) {
        // ... (construtor e outros métodos permanecem iguais) ...
        this.authService = appContext.getAuthService();
        this.vendaService = appContext.getVendaService();
        this.clienteService = appContext.getClienteService();
        this.produtoService = appContext.getProdutoService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);

        carrinhoTableModel = new DefaultTableModel(new String[]{"Produto", "Lote", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable carrinhoTable = new JTable(carrinhoTableModel);
        add(new JScrollPane(carrinhoTable), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        carregarDadosIniciais();
    }

    // ... (métodos carregarDadosIniciais, atualizarLotesDisponiveis permanecem iguais) ...
    private void carregarDadosIniciais() {
        clienteComboBox.removeAllItems();
        // Adiciona um cliente "Nulo" ou "Consumidor Final" como opção
        clienteComboBox.addItem(null); // Permite vendas sem cliente selecionado
        clienteService.listarTodos().forEach(clienteComboBox::addItem);

        produtoComboBox.removeAllItems();
        produtoService.listarProdutosAtivosParaVenda().forEach(produtoComboBox::addItem);

        atualizarLotesDisponiveis();
    }

    private void atualizarLotesDisponiveis() {
        loteComboBox.removeAllItems();
        Produto produtoSelecionado = (Produto) produtoComboBox.getSelectedItem();
        if (produtoSelecionado != null) {
            produtoService.buscarLotesDisponiveis(produtoSelecionado.getId()).forEach(loteComboBox::addItem);
        }
    }

    private void adicionarAoCarrinho() {
        Lote loteSelecionado = (Lote) loteComboBox.getSelectedItem();
        if (loteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto e um lote válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade = (Integer) quantidadeSpinner.getValue();
        if (quantidade > loteSelecionado.getQuantidade()) {
            JOptionPane.showMessageDialog(this, "Quantidade solicitada excede o estoque do lote (" + loteSelecionado.getQuantidade() + ").", "Erro de Estoque", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cria o item de venda
        VendaItem novoItem = new VendaItem(loteSelecionado, quantidade);
        carrinho.add(novoItem);
        atualizarCarrinho();
    }

    private void atualizarCarrinho() {
        carrinhoTableModel.setRowCount(0);
        double total = 0.0;
        for (VendaItem item : carrinho) {
            double subtotal = item.getPrecoUnitario() * item.getQuantidade();
            String nomeProduto = item.getLote().getProduto().getNome();
            carrinhoTableModel.addRow(new Object[]{
                    nomeProduto,
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
        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O carrinho está vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CORREÇÃO: Obtém os objetos completos, não apenas os IDs
        Cliente clienteSelecionado = (Cliente) clienteComboBox.getSelectedItem();
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        // Calcula o valor total e cria o objeto Venda
        double valorTotal = carrinho.stream().mapToDouble(item -> item.getQuantidade() * item.getPrecoUnitario()).sum();
        Venda novaVenda = new Venda(clienteSelecionado, ator, valorTotal);

        // Adiciona os itens à venda
        for (VendaItem item : carrinho) {
            novaVenda.adicionarItem(item);
        }

        try {
            // O serviço agora recebe o objeto Venda completo
            vendaService.finalizarVenda(novaVenda, ator);
            JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparVenda();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro ao finalizar a venda:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTopPanel() {
        // ... (código do painel permanece o mesmo) ...
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nova Venda"));

        JPanel selectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        clienteComboBox = new JComboBox<>();
        produtoComboBox = new JComboBox<>();
        loteComboBox = new JComboBox<>();
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
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
        // ... (código do painel permanece o mesmo) ...
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: " + currencyFormat.format(0.0));
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