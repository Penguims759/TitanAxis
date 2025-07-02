package com.titanaxis.view.panels;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientePanel extends BaseCrudPanel<Cliente> {

    private final ClienteService clienteService;
    private final AuthService authService;

    private JTextField idField, nomeField, contatoField, enderecoField;

    public ClientePanel(AppContext appContext) {
        super();
        this.authService = appContext.getAuthService();
        this.clienteService = appContext.getClienteService();

        setupListeners();
        loadData();
    }

    @Override
    protected JPanel createFormPanel() {
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

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Nome", "Contato", "Endereço"};
    }

    @Override
    protected void populateTable(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        clientes.forEach(cliente -> tableModel.addRow(new Object[]{
                cliente.getId(), cliente.getNome(), cliente.getContato(), cliente.getEndereco()
        }));
    }

    @Override
    protected void displaySelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nomeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            contatoField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            enderecoField.setText(tableModel.getValueAt(selectedRow, 3).toString());
        }
    }

    @Override
    protected void clearFields() {
        idField.setText("");
        nomeField.setText("");
        contatoField.setText("");
        enderecoField.setText("");
        table.clearSelection();
    }

    @Override
    protected void loadData() {
        populateTable(clienteService.listarTodos());
    }

    @Override
    protected void onSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            populateTable(clienteService.buscarPorNome(searchTerm));
        } else {
            loadData();
        }
    }

    @Override
    protected void onSave() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do cliente é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isUpdate = !idField.getText().isEmpty();
        int id = isUpdate ? Integer.parseInt(idField.getText()) : 0;
        String contato = contatoField.getText().trim();
        String endereco = enderecoField.getText().trim();

        Cliente cliente = new Cliente(id, nome, contato, endereco);
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            clienteService.salvar(cliente, ator);
            JOptionPane.showMessageDialog(this, "Cliente " + (isUpdate ? "atualizado" : "adicionado") + " com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onDelete() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idField.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja eliminar este cliente?", "Confirmar Eliminação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Usuario ator = authService.getUsuarioLogado().orElse(null);
            try {
                clienteService.deletar(id, ator);
                JOptionPane.showMessageDialog(this, "Cliente eliminado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                clearFields();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao eliminar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}