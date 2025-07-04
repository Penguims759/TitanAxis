// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/VendaPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.exception.CarrinhoVazioException; // Importado
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException; // Importado
import com.titanaxis.model.*;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.service.ProdutoService;
import com.titanaxis.service.VendaService;
import com.titanaxis.util.UIMessageUtil; // Importado
import com.titanaxis.util.AppLogger; // Importado

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level; // Importado
import java.util.logging.Logger; // Importado

public class VendaPanel extends JPanel {

    private final VendaService vendaService;
    private final AuthService authService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    private final DefaultTableModel carrinhoTableModel; // Adicionado final
    private final JComboBox<Cliente> clienteComboBox; // Adicionado final
    private final JComboBox<Produto> produtoComboBox; // Adicionado final
    private final JComboBox<Lote> loteComboBox; // Adicionado final
    private final JSpinner quantidadeSpinner; // Adicionado final
    private final JLabel totalLabel; // Adicionado final

    private final List<VendaItem> carrinho = new ArrayList<>(); // Adicionado final
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")); // Adicionado final
    private static final Logger logger = AppLogger.getLogger(); // Adicionado para logging

    public VendaPanel(AppContext appContext) {
        this.authService = appContext.getAuthService();
        this.vendaService = appContext.getVendaService();
        this.clienteService = appContext.getClienteService();
        this.produtoService = appContext.getProdutoService();

        // Inicialização de componentes final no construtor
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
        JTable carrinhoTable = new JTable(carrinhoTableModel); // Uso do tableModel inicializado
        add(new JScrollPane(carrinhoTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        carregarDadosIniciais();
    }

    private void carregarDadosIniciais() {
        try {
            clienteComboBox.removeAllItems();
            clienteComboBox.addItem(null);
            clienteService.listarTodos().forEach(clienteComboBox::addItem);

            produtoComboBox.removeAllItems();
            produtoService.listarProdutosAtivosParaVenda().forEach(produtoComboBox::addItem);

            atualizarLotesDisponiveis();
        } catch (PersistenciaException e) {
            logger.log(Level.SEVERE, "Erro ao carregar dados iniciais de venda.", e); // Log do erro
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
                logger.log(Level.SEVERE, "Erro ao carregar lotes para o produto " + produtoSelecionado.getNome(), e); // Log do erro
                UIMessageUtil.showErrorMessage(this, "Erro ao carregar lotes: " + e.getMessage(), "Erro de Base de Dados");
            }
        }
    }

    private void adicionarAoCarrinho() {
        logger.info("Tentando adicionar item ao carrinho."); // NOVO LOG
        Lote loteSelecionado = (Lote) loteComboBox.getSelectedItem();
        int quantidade = (Integer) quantidadeSpinner.getValue();

        logger.info("Lote selecionado: " + (loteSelecionado != null ? loteSelecionado.getNumeroLote() : "null") + ", Quantidade: " + quantidade); // NOVO LOG

        if (loteSelecionado == null) {
            UIMessageUtil.showWarningMessage(this, "Selecione um produto e um lote válido.", "Aviso");
            logger.warning("Falha ao adicionar item ao carrinho: Nenhum lote selecionado."); // NOVO LOG
            return;
        }

        if (quantidade <= 0) { // Adicionada validação explícita para quantidade positiva
            UIMessageUtil.showErrorMessage(this, "A quantidade deve ser maior que zero.", "Erro de Quantidade");
            logger.warning("Falha ao adicionar item ao carrinho: Quantidade é zero ou menor."); // NOVO LOG
            return;
        }

        if (quantidade > loteSelecionado.getQuantidade()) {
            UIMessageUtil.showErrorMessage(this, "Quantidade solicitada excede o estoque do lote (" + loteSelecionado.getQuantidade() + ").", "Erro de Estoque");
            logger.warning("Falha ao adicionar item ao carrinho: Estoque insuficiente para o lote " + loteSelecionado.getNumeroLote() + ". Solicitado: " + quantidade + ", Disponível: " + loteSelecionado.getQuantidade()); // NOVO LOG
            return;
        }

        VendaItem novoItem = new VendaItem(loteSelecionado, quantidade);
        carrinho.add(novoItem);
        atualizarCarrinho();
        UIMessageUtil.showInfoMessage(this, "Item adicionado ao carrinho: " + loteSelecionado.getProduto().getNome() + " x" + quantidade, "Sucesso");
        logger.info("Item adicionado com sucesso ao carrinho: " + loteSelecionado.getProduto().getNome() + " (Lote: " + loteSelecionado.getNumeroLote() + ") x" + quantidade); // NOVO LOG
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
        quantidadeSpinner.setValue(1); // Resetar quantidade para 1 após adicionar
    }

    private void limparVenda() {
        carrinho.clear();
        atualizarCarrinho();
        if (clienteComboBox.getItemCount() > 0) clienteComboBox.setSelectedIndex(0);
        carregarDadosIniciais(); // Recarrega produtos/lotes para refletir mudanças de estoque
    }

    private void finalizarVenda() {
        // As validações iniciais de carrinho vazio ou utilizador não autenticado devem ser feitas aqui ou no Service.
        // O Service já faz a validação de usuário logado e carrinho vazio (agora com CarrinhoVazioException).

        Cliente clienteSelecionado = (Cliente) clienteComboBox.getSelectedItem();
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        double valorTotal = carrinho.stream().mapToDouble(item -> item.getQuantidade() * item.getPrecoUnitario()).sum();
        Venda novaVenda = new Venda(clienteSelecionado, ator, valorTotal);
        carrinho.forEach(novaVenda::adicionarItem);

        try {
            vendaService.finalizarVenda(novaVenda, ator);
            UIMessageUtil.showInfoMessage(this, "Venda finalizada com sucesso!", "Sucesso");
            limparVenda();
        } catch (CarrinhoVazioException e) { // ALTERADO: Captura exceção específica
            logger.log(Level.WARNING, "Tentativa de finalizar venda com carrinho vazio.", e); // Log do aviso
            UIMessageUtil.showWarningMessage(this, e.getMessage(), "Aviso");
        } catch (UtilizadorNaoAutenticadoException e) { // ALTERADO: Captura exceção específica
            logger.log(Level.WARNING, "Tentativa de finalizar venda sem utilizador autenticado.", e); // Log do aviso
            UIMessageUtil.showErrorMessage(this, e.getMessage(), "Erro de Autenticação");
        } catch (PersistenciaException e) { // ALTERADO: Captura exceção geral de persistência
            // PersistenciaException agora encapsula erros como "Estoque insuficiente"
            logger.log(Level.SEVERE, "Erro de base de dados ao finalizar a venda. Causa: " + e.getMessage(), e); // Log completo
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro de base de dados ao finalizar a venda:\n" + e.getMessage(), "Erro de Persistência");
        } catch (Exception e) { // Catch-all para qualquer outra exceção inesperada
            logger.log(Level.SEVERE, "Erro inesperado ao finalizar a venda.", e); // Log completo
            UIMessageUtil.showErrorMessage(this, "Ocorreu um erro inesperado ao finalizar a venda. Consulte os logs para mais detalhes.", "Erro");
        }
    }

    // NOVO MÉTODO: Para ser chamado externamente (e.g., pelo DashboardFrame) para recarregar os dados
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
        // ComboBoxes e Spinner já inicializados no construtor
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
        // totalLabel já inicializado no construtor
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