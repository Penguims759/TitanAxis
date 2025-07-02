package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class UsuarioPanel extends BaseCrudPanel<Usuario> {

    private final AuthService authService;

    private JTextField idField, usernameField;
    private JPasswordField passwordField;
    private JComboBox<NivelAcesso> nivelAcessoComboBox;

    public UsuarioPanel(AppContext appContext) {
        super();
        this.authService = appContext.getAuthService();

        setupListeners();
        loadData();
    }

    @Override
    protected JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes do Utilizador"));

        idField = new JTextField();
        idField.setEditable(false);
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        nivelAcessoComboBox = new JComboBox<>(NivelAcesso.values());

        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nome de Utilizador:"));
        panel.add(usernameField);
        panel.add(new JLabel("Nova Senha (deixe em branco para não alterar):"));
        panel.add(passwordField);
        panel.add(new JLabel("Nível de Acesso:"));
        panel.add(nivelAcessoComboBox);

        return panel;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Nome de Utilizador", "Nível de Acesso"};
    }

    @Override
    protected void populateTable(List<Usuario> usuarios) {
        tableModel.setRowCount(0);
        usuarios.forEach(u -> tableModel.addRow(new Object[]{u.getId(), u.getNomeUsuario(), u.getNivelAcesso()}));
    }

    @Override
    protected void displaySelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            nivelAcessoComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 2));
            passwordField.setText("");
        }
    }

    @Override
    protected void clearFields() {
        idField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        nivelAcessoComboBox.setSelectedIndex(0);
        table.clearSelection();
    }

    @Override
    protected void loadData() {
        populateTable(authService.listarUsuarios());
    }

    @Override
    protected void onSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            populateTable(authService.buscarUsuariosPorNomeContendo(searchTerm));
        } else {
            loadData();
        }
    }

    @Override
    protected void onSave() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        NivelAcesso nivelAcesso = (NivelAcesso) nivelAcessoComboBox.getSelectedItem();

        boolean isUpdate = !idField.getText().isEmpty();

        if (username.isEmpty() || nivelAcesso == null || (!isUpdate && password.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Nome, senha (para novos utilizadores) e nível de acesso são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = isUpdate ? Integer.parseInt(idField.getText()) : 0;
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        if (isUpdate) {
            Optional<Usuario> userOpt = authService.listarUsuarios().stream().filter(u -> u.getId() == id).findFirst();
            if (userOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Utilizador não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String senhaHash = password.isEmpty() ? userOpt.get().getSenhaHash() : PasswordUtil.hashPassword(password);
            Usuario usuarioAtualizado = new Usuario(id, username, senhaHash, nivelAcesso);

            if (authService.atualizarUsuario(usuarioAtualizado, ator)) {
                JOptionPane.showMessageDialog(this, "Utilizador atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar utilizador.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else { // É um novo utilizador
            if (authService.cadastrarUsuario(username, password, nivelAcesso, ator)) {
                JOptionPane.showMessageDialog(this, "Utilizador adicionado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao adicionar. O nome de utilizador pode já existir.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        loadData();
        clearFields();
    }

    @Override
    protected void onDelete() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um utilizador para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
            loadData();
            clearFields();
        }
    }
}