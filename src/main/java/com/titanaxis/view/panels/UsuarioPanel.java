package com.titanaxis.view.panels;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.PasswordUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UsuarioPanel extends JPanel {
    private final AuthService authService;
    private final DefaultTableModel tableModel;
    private final JTable usuarioTable;
    private final JTextField idField, usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<NivelAcesso> nivelAcessoComboBox;
    private static final Logger logger = AppLogger.getLogger();

    public UsuarioPanel(AuthService authService) {
        this.authService = authService;
        setLayout(new BorderLayout(10, 10));

        // Formulário
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalhes do Utilizador"));
        idField = new JTextField();
        idField.setEditable(false);
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        nivelAcessoComboBox = new JComboBox<>(NivelAcesso.values());

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Nome de Utilizador:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Nova Senha (deixe em branco para não alterar):"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Nível de Acesso:"));
        formPanel.add(nivelAcessoComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Adicionar");
        JButton updateButton = new JButton("Atualizar");
        JButton deleteButton = new JButton("Eliminar");
        JButton clearButton = new JButton("Limpar");
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Tabela e Busca
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(25);
        JButton searchButton = new JButton("Buscar");
        searchPanel.add(new JLabel("Buscar por Nome:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        String[] columnNames = {"ID", "Nome de Utilizador", "Nível de Acesso"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        usuarioTable = new JTable(tableModel);
        usuarioTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) displaySelectedUser();
        });

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(searchPanel, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(usuarioTable), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // Ações
        addButton.addActionListener(e -> addUsuario());
        updateButton.addActionListener(e -> updateUsuario());
        deleteButton.addActionListener(e -> deleteUsuario());
        clearButton.addActionListener(e -> clearFields());
        searchButton.addActionListener(e -> performSearch(searchField.getText()));
        searchField.addActionListener(e -> performSearch(searchField.getText()));

        loadUsuarios();
    }

    private void popularTabela(List<Usuario> usuarios) {
        tableModel.setRowCount(0);
        usuarios.forEach(u -> tableModel.addRow(new Object[]{u.getId(), u.getNomeUsuario(), u.getNivelAcesso()}));
    }

    private void performSearch(String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            popularTabela(authService.buscarUsuariosPorNomeContendo(searchTerm));
        } else {
            loadUsuarios();
        }
    }

    private void loadUsuarios() {
        popularTabela(authService.listarUsuarios());
    }

    private void displaySelectedUser() {
        int selectedRow = usuarioTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            nivelAcessoComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 2));
            passwordField.setText("");
        }
    }

    private void addUsuario() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        NivelAcesso nivelAcesso = (NivelAcesso) nivelAcessoComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || nivelAcesso == null) {
            JOptionPane.showMessageDialog(this, "Nome, senha e nível de acesso são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (authService.cadastrarUsuario(username, password, nivelAcesso, ator)) {
            JOptionPane.showMessageDialog(this, "Usuário adicionado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadUsuarios();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar. O nome de utilizador pode já existir.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUsuario() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para atualizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idField.getText());
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        NivelAcesso nivelAcesso = (NivelAcesso) nivelAcessoComboBox.getSelectedItem();

        if (username.isEmpty() || nivelAcesso == null) {
            JOptionPane.showMessageDialog(this, "Nome e nível de acesso são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Optional<Usuario> userOpt = authService.listarUsuarios().stream().filter(u -> u.getId() == id).findFirst();
        if(userOpt.isEmpty()){
            JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String senhaHash = password.isEmpty() ? userOpt.get().getSenhaHash() : PasswordUtil.hashPassword(password);
        Usuario usuarioAtualizado = new Usuario(id, username, senhaHash, nivelAcesso);

        Usuario ator = authService.getUsuarioLogado().orElse(null);
        if (authService.atualizarUsuario(usuarioAtualizado, ator)) {
            JOptionPane.showMessageDialog(this, "Usuário atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadUsuarios();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUsuario() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idToDelete = Integer.parseInt(idField.getText());
        if (authService.getUsuarioLogadoId() == idToDelete) {
            JOptionPane.showMessageDialog(this, "Não pode eliminar o seu próprio utilizador.", "Ação Inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            authService.deletarUsuario(idToDelete, ator);
            JOptionPane.showMessageDialog(this, "Utilizador eliminado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadUsuarios();
            clearFields();
        }
    }

    private void clearFields() {
        idField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        nivelAcessoComboBox.setSelectedIndex(0);
        usuarioTable.clearSelection();
    }
}