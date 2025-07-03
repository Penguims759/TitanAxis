package com.titanaxis.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    // CORREÇÃO: Especificamos a definição exata da coluna para corresponder ao script SQL.
    @Column(name = "preco", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double preco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private boolean ativo;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lote> lotes = new ArrayList<>();

    @Transient
    private int quantidadeTotal;

    @Transient
    private String nomeCategoria;

    // Construtores
    public Produto() {}

    public Produto(String nome, String descricao, double preco, Categoria categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
        this.ativo = true;
    }

    public void addLote(Lote lote) {
        this.lotes.add(lote);
        lote.setProduto(this);
    }

    // --- Getters e Setters (sem alterações) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }

    public int getQuantidadeTotal() {
        if (this.lotes == null) {
            return 0;
        }
        return this.lotes.stream().mapToInt(Lote::getQuantidade).sum();
    }
    public void setQuantidadeTotal(int quantidadeTotal) { this.quantidadeTotal = quantidadeTotal; }

    public String getNomeCategoria() {
        return (categoria != null) ? categoria.getNome() : null;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}