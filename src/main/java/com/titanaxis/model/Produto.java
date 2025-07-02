// src/main/java/com/titanaxis/model/Produto.java
package com.titanaxis.model;

import java.util.List;

public class Produto {
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int categoriaId;
    private String nomeCategoria;
    private boolean ativo; // <-- NOVO CAMPO

    // Campos transitórios
    private int quantidadeTotal;
    private List<Lote> lotes;

    // Construtor principal para ler do banco de dados
    public Produto(int id, String nome, String descricao, double preco, int categoriaId, String nomeCategoria, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.nomeCategoria = nomeCategoria;
        this.ativo = ativo;
    }

    // Construtor para criar novos produtos na UI
    public Produto(String nome, String descricao, double preco, int categoriaId) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.ativo = true; // Novos produtos são ativos por defeito
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }
    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }
    public boolean isAtivo() { return ativo; } // <-- NOVO
    public void setAtivo(boolean ativo) { this.ativo = ativo; } // <-- NOVO

    public int getQuantidadeTotal() { return quantidadeTotal; }
    public void setQuantidadeTotal(int quantidadeTotal) { this.quantidadeTotal = quantidadeTotal; }
    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }

    @Override
    public String toString() {
        return this.nome;
    }
}