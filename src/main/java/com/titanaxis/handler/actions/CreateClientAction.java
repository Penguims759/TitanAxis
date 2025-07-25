package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Cliente;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;
import java.util.Map;

public class CreateClientAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public CreateClientAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            Cliente novoCliente = new Cliente((String) params.get("nome"), (String) params.get("contato"), "");
            appContext.getClienteService().salvar(novoCliente, ator);
            UIMessageUtil.showInfoMessage(frame, I18n.getString("dashboard.action.clientCreated", novoCliente.getNome()), I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Clientes");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}