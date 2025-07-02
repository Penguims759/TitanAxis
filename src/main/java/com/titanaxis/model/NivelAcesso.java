package com.titanaxis.model;

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
        // Isto garante que o JComboBox exibe o nome de forma amigável
        return this.nome;
    }
}