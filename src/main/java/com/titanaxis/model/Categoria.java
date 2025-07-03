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

    // Relação Um-para-Muitos: Uma categoria pode ter muitos produtos.
    // 'mappedBy = "categoria"' diz ao Hibernate que a relação já está mapeada pelo campo 'categoria' na classe Produto.
    // CascadeType.ALL significa que as operações (salvar, apagar) na Categoria se propagam para os Produtos associados.
    // orphanRemoval = true garante que, se um produto for removido desta lista, ele será apagado da base de dados.
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

    // Agora podemos calcular o total de produtos diretamente da lista
    public int getTotalProdutos() {
        if (this.produtos != null) {
            return this.produtos.size();
        }
        // Se a lista não foi carregada, retorna o valor que foi calculado pela query
        return this.totalProdutos;
    }
    public void setTotalProdutos(int totalProdutos) { this.totalProdutos = totalProdutos; }

    @Override
    public String toString() {
        return nome;
    }
}