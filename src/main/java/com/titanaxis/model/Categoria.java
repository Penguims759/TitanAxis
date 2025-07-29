package com.titanaxis.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Produto> produtos = new ArrayList<>();

    @Transient
    private int totalProdutos;

    // Construtores
    public Categoria() {}

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public Categoria(int id, String nome, int totalProdutos) {
        this.id = id;
        this.nome = nome;
        this.totalProdutos = totalProdutos;
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public List<Produto> getProdutos() { return produtos; }
    public void setProdutos(List<Produto> produtos) { this.produtos = produtos; }

    // CORREÇÃO APLICADA AQUI
    // Agora o método retorna diretamente o valor pré-calculado pela query,
    // que é a forma mais eficiente e correta neste contexto.
    public int getTotalProdutos() {
        return this.totalProdutos;
    }

    public void setTotalProdutos(int totalProdutos) { this.totalProdutos = totalProdutos; }

    @Override
    public String toString() {
        return nome;
    }
}