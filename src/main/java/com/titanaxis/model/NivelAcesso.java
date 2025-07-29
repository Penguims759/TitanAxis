package com.titanaxis.model;

import com.titanaxis.util.I18n; // Importado

public enum NivelAcesso {
    PADRAO("padrao"),
    GERENTE("gerente"),
    ADMIN("admin");

    private final String nome;

    NivelAcesso(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        // ALTERADO: Busca o nome de exibição do arquivo de propriedades
        return switch (this) {
            case PADRAO -> I18n.getString("accesslevel.standard");
            case GERENTE -> I18n.getString("accesslevel.manager");
            case ADMIN -> I18n.getString("accesslevel.admin");
        };
    }
}