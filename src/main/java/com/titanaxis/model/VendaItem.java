// src/main/java/com/titanaxis/model/VendaItem.java
package com.titanaxis.model;

public class VendaItem {
    private int id;
    private int vendaId;
    private Lote lote; // <-- ALTERAÇÃO: Agora guarda o Lote específico
    private int quantidade;
    private double precoUnitario;

    public VendaItem(Lote lote, int quantidade) {
        this.lote = lote;
        this.quantidade = quantidade;
        // O preço ainda vem do produto genérico, associado ao lote
        // (Futuramente, o preço poderia até estar no lote, se houver promoções)
        this.precoUnitario = 0; // Será definido ao buscar o produto
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVendaId() { return vendaId; }
    public void setVendaId(int vendaId) { this.vendaId = vendaId; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
}