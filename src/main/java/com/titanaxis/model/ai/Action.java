package com.titanaxis.model.ai;

public enum Action {
    // Ações de Guia Visual
    GUIDE_NAVIGATE_TO_ADD_LOTE,
    GUIDE_NAVIGATE_TO_ADD_PRODUCT, // NOVA AÇÃO

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
    UI_NAVIGATE,

    // Ação especial para indicar que o assistente aguarda uma resposta do utilizador
    AWAITING_INFO,

    // Ação para exibir texto complexo no chat
    DISPLAY_COMPLEX_RESPONSE
}