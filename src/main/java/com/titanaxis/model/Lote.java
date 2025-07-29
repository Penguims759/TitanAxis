package com.titanaxis.model;

import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "estoque_lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "numero_lote", nullable = false)
    private String numeroLote;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    public Lote() {}

    public Lote(int id, Produto produto, String numeroLote, int quantidade, LocalDate dataValidade) {
        this.id = id;
        this.produto = produto;
        this.numeroLote = numeroLote;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
    }

    public Lote(Produto produto, String numeroLote, int quantidade, LocalDate dataValidade) {
        this.produto = produto;
        this.numeroLote = numeroLote;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public int getProdutoId() {
        return (produto != null) ? produto.getId() : 0;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String validadeFormatada = (dataValidade != null) ? dataValidade.format(formatter) : I18n.getString("general.notAvailable");
        
        return I18n.getString("renderer.lote.format", numeroLote, validadeFormatada, quantidade);
    }
}