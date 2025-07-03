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

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double precoUnitario;

    // ... (resto da classe sem alterações) ...
    public VendaItem() {}

    public VendaItem(Lote lote, int quantidade) {
        this.lote = lote;
        this.quantidade = quantidade;
        if (lote != null && lote.getProduto() != null) {
            this.precoUnitario = lote.getProduto().getPreco();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
}