// src/main/java/com/titanaxis/model/ai/Action.java
package com.titanaxis.model.ai;

public enum Action {
    // Ações de Guia Visual
    GUIDE_NAVIGATE_TO_ADD_LOTE,

    // Ações de Execução Direta
    DIRECT_CREATE_CLIENT,
    DIRECT_GENERATE_SALES_REPORT_PDF,

    // Ações de Personalização
    UI_CHANGE_THEME,

    // Ações que requerem mais dados do usuário
    AWAITING_INFO
}