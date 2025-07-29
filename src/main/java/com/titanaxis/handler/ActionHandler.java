package com.titanaxis.handler;

import com.titanaxis.app.AppContext;
import com.titanaxis.handler.actions.*;
import com.titanaxis.model.ai.Action;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import com.titanaxis.util.UIMessageUtil;
import com.titanaxis.view.DashboardFrame;

import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;

public class ActionHandler {
    private final Map<Action, DashboardAction> actionMap = new EnumMap<>(Action.class);
    private static final Logger logger = AppLogger.getLogger();

    public ActionHandler(DashboardFrame frame, AppContext appContext) {
        // Ações de Navegação e UI
        actionMap.put(Action.UI_NAVIGATE, new NavigateAction(frame));
        actionMap.put(Action.UI_CHANGE_THEME, new ChangeThemeAction(frame));

        // Ações de Guia Visual
        actionMap.put(Action.GUIDE_NAVIGATE_TO_ADD_LOTE, new GuideAddLoteAction(frame));
        actionMap.put(Action.GUIDE_NAVIGATE_TO_ADD_PRODUCT, new GuideAddProductAction(frame));

        // Ações de Execução Direta
        actionMap.put(Action.DIRECT_CREATE_PRODUCT, new CreateProductAction(appContext, frame));
        actionMap.put(Action.DIRECT_CREATE_CLIENT, new CreateClientAction(appContext, frame));
        actionMap.put(Action.DIRECT_CREATE_FORNECEDOR, new CreateFornecedorAction(appContext, frame));
        actionMap.put(Action.DIRECT_CREATE_CATEGORY, new CreateCategoryAction(appContext, frame));
        actionMap.put(Action.DIRECT_CREATE_USER, new CreateUserAction(appContext, frame));
        actionMap.put(Action.DIRECT_UPDATE_PRODUCT, new UpdateProductAction(appContext, frame));
        actionMap.put(Action.DIRECT_ADD_STOCK, new AddStockAction(appContext, frame));
        actionMap.put(Action.DIRECT_ADJUST_STOCK, new AdjustStockAction(appContext, frame));
        actionMap.put(Action.DIRECT_GENERATE_SALES_REPORT_PDF, new GenerateReportAction(frame));
        actionMap.put(Action.START_SALE_FOR_CLIENT, new StartSaleForClientAction(frame));
    }

    public void execute(Action action, Map<String, Object> params) {
        DashboardAction command = actionMap.get(action);
        if (command != null) {
            try {
                command.execute(params);
            } catch (Exception e) {
                UIMessageUtil.showErrorMessage(null, I18n.getString("dashboard.action.errorExecuting", e.getMessage()), I18n.getString("dashboard.action.errorTitle"));
                logger.error("Erro detalhado ao executar a ação '" + action + "': ", e);
            }
        } else {
            logger.warn("Ação não mapeada no ActionHandler: " + action);
            UIMessageUtil.showWarningMessage(null, "A ação '" + action + "' ainda não foi implementada.", "Funcionalidade Futura");
        }
    }
}