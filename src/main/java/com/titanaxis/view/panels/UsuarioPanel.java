// File: penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/view/panels/UsuarioPanel.java
package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.presenter.UsuarioPresenter;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.interfaces.UsuarioView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuarioPanel extends JPanel implements UsuarioView {

    private UsuarioViewListener listener;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField usernameField;
    private final JTextField searchField;
    private final JPasswordField passwordField;
    private final JComboBox<NivelAcesso> nivelAcessoComboBox;

    public UsuarioPanel(AppContext appContext) {
        idField = new JTextField();
        idField.setEditable(false);
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        nivelAcessoComboBox = new JComboBox<>(NivelAcesso.values());
        searchField = new JTextField(25);

        // ALTERADO
        tableModel = new DefaultTableModel(new String[]{
                I18n.getString("user.table.header.id"),
                I18n.getString("user.table.header.name"),
                I18n.getString("user.table.header.accessLevel")
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        initComponents();
        new UsuarioPresenter(this, appContext.getAuthService());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createFormPanel(), BorderLayout.CENTER);
        northPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(createSearchPanel(searchField), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                int id = (int) tableModel.getValueAt(modelRow, 0);
                String nome = (String) tableModel.getValueAt(modelRow, 1);
                NivelAcesso nivel = (NivelAcesso) tableModel.getValueAt(modelRow, 2);
                listener.aoSelecionarUsuario(new Usuario(id, nome, "", nivel));
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(I18n.getString("user.border.details"))); // ALTERADO
        panel.add(new JLabel(I18n.getString("user.label.id"))); // ALTERADO
        panel.add(idField);
        panel.add(new JLabel(I18n.getString("user.label.username"))); // ALTERADO
        panel.add(usernameField);
        panel.add(new JLabel(I18n.getString("user.label.newPassword"))); // ALTERADO
        panel.add(passwordField);
        panel.add(new JLabel(I18n.getString("user.label.accessLevel"))); // ALTERADO
        panel.add(nivelAcessoComboBox);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton(I18n.getString("user.button.save")); // ALTERADO
        JButton deleteButton = new JButton(I18n.getString("user.button.delete")); // ALTERADO
        JButton clearButton = new JButton(I18n.getString("user.button.clear")); // ALTERADO
        saveButton.addActionListener(e -> listener.aoSalvar());
        deleteButton.addActionListener(e -> listener.aoApagar());
        clearButton.addActionListener(e -> listener.aoLimpar());
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JPanel createSearchPanel(JTextField searchField) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton(I18n.getString("user.button.search")); // ALTERADO
        JButton clearSearchButton = new JButton(I18n.getString("user.button.clearSearch")); // ALTERADO
        searchPanel.add(new JLabel(I18n.getString("user.label.searchByName"))); // ALTERADO
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        searchButton.addActionListener(e -> listener.aoBuscar());
        searchField.addActionListener(e -> listener.aoBuscar());
        clearSearchButton.addActionListener(e -> listener.aoLimparBusca());
        return searchPanel;
    }

    @Override
    public String getId() { return idField.getText(); }

    @Override
    public String getUsername() { return usernameField.getText(); }

    @Override
    public String getPassword() { return new String(passwordField.getPassword()); }

    @Override
    public NivelAcesso getNivelAcesso() { return (NivelAcesso) nivelAcessoComboBox.getSelectedItem(); }

    @Override
    public void setId(String id) { idField.setText(id); }

    @Override
    public void setUsername(String username) { usernameField.setText(username); }

    @Override
    public void setPassword(String password) { passwordField.setText(password); }

    @Override
    public void setNivelAcesso(NivelAcesso nivel) { nivelAcessoComboBox.setSelectedItem(nivel); }

    @Override
    public void setUsuariosNaTabela(List<Usuario> usuarios) {
        tableModel.setRowCount(0);
        usuarios.forEach(u -> tableModel.addRow(new Object[]{u.getId(), u.getNomeUsuario(), u.getNivelAcesso()}));
        table.clearSelection();
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

    @Override
    public String getTermoBusca() { return searchField.getText(); }

    @Override
    public void setTermoBusca(String termo) { searchField.setText(termo); }

    @Override
    public void clearTableSelection() { table.clearSelection(); }

    public void refreshData() {
        if (listener != null) {
            listener.aoCarregarDadosIniciais();
            table.clearSelection();
        }
    }

    @Override
    public void setListener(UsuarioViewListener listener) { this.listener = listener; }
}