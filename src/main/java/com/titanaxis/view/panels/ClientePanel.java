// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/view/panels/ClientePanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
import com.titanaxis.presenter.ClientePresenter;
import com.titanaxis.util.UIMessageUtil; // Importado
import com.titanaxis.view.interfaces.ClienteView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel implements ClienteView {

    private ClienteViewListener listener;
    private final DefaultTableModel tableModel; // Adicionado final
    private final JTable table; // Adicionado final
    private final JTextField idField; // Adicionado final
    private final JTextField nomeField; // Adicionado final
    private final JTextField contatoField; // Adicionado final
    private final JTextField enderecoField; // Adicionado final
    private final JTextField searchField; // Adicionado final

    public ClientePanel(AppContext appContext) {
        // ALTERADO: Inicialização de campos 'final' movida para o construtor.
        searchField = new JTextField(25);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Contato", "Endereço"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFocusable(false); // NOVO: Remove o foco visual da tabela
        table.setSelectionBackground(table.getBackground()); // NOVO: Torna o fundo da seleção invisível
        table.setSelectionForeground(table.getForeground()); // NOVO: Mantém a cor do texto da seleção

        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        contatoField = new JTextField();
        enderecoField = new JTextField();

        initComponents(); // Chama o método para construir o layout com os componentes já inicializados
        new ClientePresenter(this, appContext.getClienteService(), appContext.getAuthService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL NORTE (Formulário e Botões) ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFormPanel(), BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (Tabela e Busca) ---
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        // searchField já inicializado no construtor
        centerPanel.add(createSearchPanel(), BorderLayout.NORTH);

        // tableModel e table já inicializados no construtor
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Listener para seleção na tabela
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                int id = (int) tableModel.getValueAt(modelRow, 0);
                String nome = (String) tableModel.getValueAt(modelRow, 1);
                String contato = (String) tableModel.getValueAt(modelRow, 2);
                String endereco = (String) tableModel.getValueAt(modelRow, 3);
                listener.aoSelecionarCliente(new Cliente(id, nome, contato, endereco));
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes do Cliente"));
        // idField, nomeField, contatoField, enderecoField já inicializados no construtor
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Contato (E-mail/Telefone):"));
        panel.add(contatoField);
        panel.add(new JLabel("Endereço:"));
        panel.add(enderecoField);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("Salvar");
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar Campos");

        saveButton.addActionListener(e -> listener.aoSalvar());
        deleteButton.addActionListener(e -> listener.aoApagar());
        clearButton.addActionListener(e -> listener.aoLimpar());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Buscar");
        JButton clearSearchButton = new JButton("Limpar Busca");

        searchPanel.add(new JLabel("Buscar por Nome:"));
        // searchField já inicializado no construtor
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        searchButton.addActionListener(e -> listener.aoBuscar());
        searchField.addActionListener(e -> listener.aoBuscar());
        clearSearchButton.addActionListener(e -> listener.aoLimparBusca());
        return searchPanel;
    }

    // --- Implementação dos métodos da interface ClienteView ---

    @Override public String getId() { return idField.getText(); }
    @Override public String getNome() { return nomeField.getText(); }
    @Override public String getContato() { return contatoField.getText(); }
    @Override public String getEndereco() { return enderecoField.getText(); }
    @Override public void setId(String id) { idField.setText(id); }
    @Override public void setNome(String nome) { nomeField.setText(nome); }
    @Override public void setContato(String contato) { contatoField.setText(contato); }
    @Override public void setEndereco(String endereco) { enderecoField.setText(endereco); }

    @Override public String getTermoBusca() { return searchField.getText(); }
    @Override public void setTermoBusca(String termo) { searchField.setText(termo); }

    @Override
    public void setClientesNaTabela(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        clientes.forEach(c -> tableModel.addRow(new Object[]{c.getId(), c.getNome(), c.getContato(), c.getEndereco()}));
        table.clearSelection(); // NOVO: Limpa a seleção da tabela
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem, boolean isErro) {
        if (isErro) {
            UIMessageUtil.showErrorMessage(this, mensagem, titulo);
        } else {
            UIMessageUtil.showInfoMessage(this, mensagem, titulo);
        }
    }

    @Override
    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        return UIMessageUtil.showConfirmDialog(this, mensagem, titulo);
    }

    // NOVO MÉTODO: Para ser chamado externamente (e.g., pelo DashboardFrame) para recarregar os dados
    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarDadosIniciais(); // Chama o método da interface do listener
            table.clearSelection(); // NOVO: Limpa a seleção da tabela
        }
    }

    @Override
    public void setListener(ClienteViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void clearTableSelection() {
        table.clearSelection();
    }
}