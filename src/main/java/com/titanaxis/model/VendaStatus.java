// src/main/java/com/titanaxis/model/VendaStatus.java
package com.titanaxis.model;

public enum VendaStatus {
    ORCAMENTO("Orçamento"), // NOVO
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String descricao;

    VendaStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}