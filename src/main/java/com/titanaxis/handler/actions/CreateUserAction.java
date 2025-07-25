package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.Map;

public class CreateUserAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public CreateUserAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            NivelAcesso level = (NivelAcesso) params.get("level");

            appContext.getAuthService().cadastrarUsuario(username, password, level, ator);
            UIMessageUtil.showInfoMessage(frame, "Utilizador '" + username + "' criado com sucesso!", I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Gestão de Usuários");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}