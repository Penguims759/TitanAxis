package com.titanaxis.service;

/**
 * Representa as intenções que o assistente de IA consegue compreender.
 * A lógica de deteção foi movida para o NLPIntentService.
 */
public enum Intent {
    CREATE_PRODUCT,
    CREATE_USER,
    CREATE_CATEGORY,
    CREATE_CLIENT,
    CREATE_FORNECEDOR,
    START_SALE,
    UPDATE_PRODUCT,
    MANAGE_STOCK,
    ADJUST_STOCK,
    QUERY_STOCK,
    QUERY_CLIENT_DETAILS,
    QUERY_PRODUCT_LOTS,
    QUERY_MOVEMENT_HISTORY,
    QUERY_TOP_CLIENTS,
    QUERY_TOP_PRODUCT,
    QUERY_CLIENT_HISTORY,
    QUERY_LOW_STOCK,
    QUERY_EXPIRING_LOTS,
    GENERATE_REPORT,
    GUIDE_ADD_LOTE,
    GUIDE_ADD_PRODUCT,
    NAVIGATE_TO,
    CHANGE_THEME,
    GREETING,
    CONFIRM,
    DENY,
    UNKNOWN;
}