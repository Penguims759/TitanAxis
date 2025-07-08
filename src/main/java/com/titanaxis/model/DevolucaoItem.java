package com.titanaxis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "devolucao_itens")
public class DevolucaoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devolucao_id", nullable = false)
    private Devolucao devolucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_item_id", nullable = false)
    private VendaItem vendaItem;

    @Column(name = "quantidade_devolvida", nullable = false)
    private int quantidadeDevolvida;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Devolucao getDevolucao() { return devolucao; }
    public void setDevolucao(Devolucao devolucao) { this.devolucao = devolucao; }
    public VendaItem getVendaItem() { return vendaItem; }
    public void setVendaItem(VendaItem vendaItem) { this.vendaItem = vendaItem; }
    public int getQuantidadeDevolvida() { return quantidadeDevolvida; }
    public void setQuantidadeDevolvida(int quantidadeDevolvida) { this.quantidadeDevolvida = quantidadeDevolvida; }
}