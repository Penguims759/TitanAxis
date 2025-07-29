package com.titanaxis.model;

import com.titanaxis.util.I18n; // Importado

public enum VendaStatus {
    ORCAMENTO("Orçamento"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String descricao;

    VendaStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        // ALTERADO: Busca a descrição do arquivo de propriedades
        return switch (this) {
            case ORCAMENTO -> I18n.getString("status.quote");
            case EM_ANDAMENTO -> I18n.getString("status.inProgress");
            case FINALIZADA -> I18n.getString("status.finalized");
            case CANCELADA -> I18n.getString("status.canceled");
        };
    }

    @Override
    public String toString() {
        return getDescricao();
    }
}