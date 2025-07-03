// FICHEIRO: src/main/java/com/titanaxis/view/panels/ClientePanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
import com.titanaxis.presenter.ClientePresenter;
import com.titanaxis.view.interfaces.ClienteView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel implements ClienteView {

    private ClienteViewListener listener;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nomeField, contatoField, enderecoField, searchField;

    public ClientePanel(AppContext appContext) {
        initComponents();
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
        searchField = new JTextField(25);
        centerPanel.add(createSearchPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Contato", "Endereço"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
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
        idField = new JTextField();
        idField.setEditable(false);
        nomeField = new JTextField();
        contatoField = new JTextField();
        enderecoField = new JTextField();
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
    }

    @Override
    public void mostrarMensagem(String titulo, String mensagem, boolean isErro) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, isErro ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        return JOptionPane.showConfirmDialog(this, mensagem, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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