package com.titanaxis.model;

/**
 * Representa a entidade Categoria no sistema.
 * Usada para categorizar os produtos.
 */
public class Categoria {
    private int id;
    private String nome;
    // --- NOVA ALTERAÇÃO: Campo para contagem de produtos ---
    private int totalProdutos;

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    // --- NOVA ALTERAÇÃO: Novo construtor para incluir a contagem ---
    public Categoria(int id, String nome, int totalProdutos) {
        this.id = id;
        this.nome = nome;
        this.totalProdutos = totalProdutos;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    // --- NOVA ALTERAÇÃO: Getter para a contagem ---
    public int getTotalProdutos() { return totalProdutos; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setTotalProdutos(int totalProdutos) { this.totalProdutos = totalProdutos; }

    @Override
    public String toString() {
        return nome;
    }
}