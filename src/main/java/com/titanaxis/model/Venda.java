package com.titanaxis.model;

import jakarta.persistence.*;
// Remova os imports do hibernate (JdbcTypeCode e SqlTypes)
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

    // A anotação @JdbcTypeCode foi removida.
    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    // ... (o resto da classe permanece igual) ...
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VendaItem> itens = new ArrayList<>();

    @Transient
    private String nomeCliente;

    public Venda() {}

    public Venda(Cliente cliente, Usuario usuario, double valorTotal) {
        this.cliente = cliente;
        this.usuario = usuario;
        this.valorTotal = valorTotal;
        this.dataVenda = LocalDateTime.now();
    }

    public Venda(int id, Cliente cliente, Usuario usuario, LocalDateTime dataVenda, double valorTotal) {
        this.id = id;
        this.cliente = cliente;
        this.usuario = usuario;
        this.dataVenda = dataVenda;
        this.valorTotal = valorTotal;
        if (cliente != null) {
            this.nomeCliente = cliente.getNome();
        }
    }

    public void adicionarItem(VendaItem item) {
        itens.add(item);
        item.setVenda(this);
    }

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

    public String getNomeCliente() {
        return (cliente != null) ? cliente.getNome() : "N/A";
    }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
}