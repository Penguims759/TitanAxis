package com.titanaxis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devolucoes")
public class Devolucao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_devolucao", nullable = false)
    private LocalDateTime dataDevolucao;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "valor_estornado", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double valorEstornado;

    @OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DevolucaoItem> itens = new ArrayList<>();

    public Devolucao() {
        this.dataDevolucao = LocalDateTime.now();
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDateTime dataDevolucao) { this.dataDevolucao = dataDevolucao; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public double getValorEstornado() { return valorEstornado; }
    public void setValorEstornado(double valorEstornado) { this.valorEstornado = valorEstornado; }
    public List<DevolucaoItem> getItens() { return itens; }
    public void setItens(List<DevolucaoItem> itens) { this.itens = itens; }
}