// src/main/java/com/titanaxis/model/Lote.java
package com.titanaxis.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Lote {
    private int id;
    private int produtoId;
    private String numeroLote;
    private int quantidade;
    private LocalDate dataValidade;

    public Lote(int id, int produtoId, String numeroLote, int quantidade, LocalDate dataValidade) {
        this.id = id;
        this.produtoId = produtoId;
        this.numeroLote = numeroLote;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
    }

    public Lote(int produtoId, String numeroLote, int quantidade, LocalDate dataValidade) {
        this.produtoId = produtoId;
        this.numeroLote = numeroLote;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String validadeFormatada = (dataValidade != null) ? dataValidade.format(formatter) : "N/A";
        return "Lote: " + numeroLote + " (Val: " + validadeFormatada + ", Qtd: " + quantidade + ")";
    }
}