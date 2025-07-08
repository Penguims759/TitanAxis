package com.titanaxis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "venda_itens")
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    // ALTERADO: Adicionado mapeamento direto para produto_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false) // 'nullable = false' conforme o schema do BD
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double precoUnitario;

    @Column(name = "desconto", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double desconto; // NOVO

    public VendaItem() {}

    // ALTERADO: Construtor para inicializar o campo 'produto' e 'desconto'
    public VendaItem(Lote lote, int quantidade) {
        this.lote = lote;
        this.quantidade = quantidade;
        this.desconto = 0.0; // NOVO
        if (lote != null && lote.getProduto() != null) {
            this.precoUnitario = lote.getProduto().getPreco();
            this.produto = lote.getProduto(); // NOVO: Inicializa o campo produto
        }
    }

    public double getSubtotal() {
        return (precoUnitario * quantidade) - desconto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    // NOVO: Getters e Setters para o campo 'produto'
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public double getDesconto() { return desconto; } // NOVO
    public void setDesconto(double desconto) { this.desconto = desconto; } // NOVO
}