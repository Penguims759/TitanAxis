// src/main/java/com/titanaxis/model/ai/Action.java
package com.titanaxis.model.ai;

public enum Action {
    // Ações de Guia Visual
    GUIDE_NAVIGATE_TO_ADD_LOTE,

    // Ações de Execução Direta
    DIRECT_CREATE_PRODUCT,
    DIRECT_CREATE_CLIENT,
    DIRECT_CREATE_CATEGORY,
    DIRECT_CREATE_USER,
    DIRECT_UPDATE_PRODUCT,
    DIRECT_ADD_STOCK,
    DIRECT_GENERATE_SALES_REPORT_PDF,
    START_SALE_FOR_CLIENT,

    // Ações de Navegação e UI
    UI_CHANGE_THEME,
    UI_NAVIGATE, // NOVO

    // Ações que requerem mais dados do usuário
    AWAITING_INFO,

    // Ação para exibir texto complexo no chat
    DISPLAY_COMPLEX_RESPONSE
}