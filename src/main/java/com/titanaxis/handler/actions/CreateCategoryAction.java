package com.titanaxis.handler.actions;

import com.titanaxis.app.AppContext;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.Map;

public class CreateCategoryAction implements DashboardAction {
    private final AppContext appContext;
    private final DashboardFrame frame;

    public CreateCategoryAction(AppContext appContext, DashboardFrame frame) {
        this.appContext = appContext;
        this.frame = frame;
    }

    @Override
    public void execute(Map<String, Object> params) {
        try {
            Usuario ator = appContext.getAuthService().getUsuarioLogado().orElse(null);
            String nomeCategoria = (String) params.get("nome");
            Categoria novaCategoria = new Categoria(nomeCategoria);

            appContext.getCategoriaService().salvar(novaCategoria, ator);
            UIMessageUtil.showInfoMessage(frame, "Categoria '" + nomeCategoria + "' criada com sucesso!", I18n.getString("dashboard.action.actionComplete"));
            frame.refreshPanel("Categorias");
        } catch (Exception e) {
            UIMessageUtil.showErrorMessage(frame, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
        }
    }
}