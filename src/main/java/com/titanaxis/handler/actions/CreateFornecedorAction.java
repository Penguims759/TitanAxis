package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Fornecedor;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import java.util.Map;

public class CreateFornecedorAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public CreateFornecedorAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            Fornecedor novoFornecedor = new Fornecedor();
            novoFornecedor.setNome((String) params.get("nome"));
            novoFornecedor.setContatoNome((String) params.get("contatoNome"));
            novoFornecedor.setContatoTelefone((String) params.get("contatoTelefone"));
            novoFornecedor.setContatoEmail((String) params.get("contatoEmail"));
            appContext.getFornecedorService().salvar(novoFornecedor, ator);
            UIMessageUtil.showInfoMessage(frame, I18n.getString("dashboard.action.supplierCreated", novoFornecedor.getNome()), I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Fornecedores");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}