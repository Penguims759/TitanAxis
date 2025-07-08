package com.titanaxis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "metas_venda")
public class MetaVenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "ano_mes", nullable = false, length = 7) // Formato "YYYY-MM"
    private String anoMes;

    @Column(name = "valor_meta", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private double valorMeta;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getAnoMes() { return anoMes; }
    public void setAnoMes(String anoMes) { this.anoMes = anoMes; }
    public double getValorMeta() { return valorMeta; }
    public void setValorMeta(double valorMeta) { this.valorMeta = valorMeta; }
}