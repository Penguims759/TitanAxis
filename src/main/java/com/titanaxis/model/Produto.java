// src/main/java/com/titanaxis/model/Produto.java
package com.titanaxis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Column(nullable = false)
    private String nome;

    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "Preço não pode exceder R$ 999.999,99")
    @Column(name = "preco", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double preco;

    @NotNull(message = "Categoria é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private boolean ativo = true;

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

    // Getters e Setters
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
    
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }
    
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }
    
    public int getQuantidadeTotal() { 
        return this.lotes.stream().mapToInt(Lote::getQuantidade).sum(); 
    }
    
    public String getNomeCategoria() { 
        return (categoria != null) ? categoria.getNome() : null; 
    }

    // Métodos de validação customizada
    @AssertTrue(message = "Produto deve ter pelo menos um lote quando ativo")
    public boolean isValidActiveProduct() {
        // Se o produto não está ativo, não precisa validar lotes
        if (!ativo) {
            return true;
        }
        // Se está ativo, deve ter pelo menos um lote (para produtos novos isso pode ser ignorado)
        return id == 0 || !lotes.isEmpty();
    }

    /**
     * Verifica se o produto tem estoque disponível.
     */
    public boolean hasStock() {
        return getQuantidadeTotal() > 0;
    }

    /**
     * Verifica se o produto tem estoque suficiente para uma quantidade específica.
     */
    public boolean hasStock(int quantidade) {
        return getQuantidadeTotal() >= quantidade;
    }

    @Override
    public String toString() {
        return this.nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return id == produto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}