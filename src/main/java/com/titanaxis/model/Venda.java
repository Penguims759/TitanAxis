// src/main/java/com/titanaxis/model/Venda.java
package com.titanaxis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "valor_total", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double valorTotal;

    @Column(name = "desconto_total", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double descontoTotal;

    @Column(name = "credito_utilizado", nullable = false, columnDefinition = "DECIMAL(10,2)") // NOVO
    private double creditoUtilizado; // NOVO

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VendaStatus status;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaItem> itens = new ArrayList<>();

    @Transient
    private String nomeCliente;

    public Venda() {
        this.dataVenda = LocalDateTime.now();
        this.status = VendaStatus.EM_ANDAMENTO;
        this.descontoTotal = 0.0;
        this.creditoUtilizado = 0.0; // NOVO
    }

    // Getters e Setters
    public double getCreditoUtilizado() { return creditoUtilizado; } // NOVO
    public void setCreditoUtilizado(double creditoUtilizado) { this.creditoUtilizado = creditoUtilizado; } // NOVO
    public double getDescontoTotal() { return descontoTotal; }
    public void setDescontoTotal(double descontoTotal) { this.descontoTotal = descontoTotal; }
    public VendaStatus getStatus() { return status; }
    public void setStatus(VendaStatus status) { this.status = status; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public List<VendaItem> getItens() { return itens; }
    public void setItens(List<VendaItem> itens) { this.itens = itens; }
    public String getNomeCliente() { return (cliente != null) ? cliente.getNome() : "N/A"; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public void adicionarItem(VendaItem item) {
        itens.add(item);
        item.setVenda(this);
    }
}