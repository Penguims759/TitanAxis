// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/model/NivelAcesso.java
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