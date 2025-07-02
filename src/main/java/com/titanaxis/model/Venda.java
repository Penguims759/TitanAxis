// src/main/java/com/titanaxis/model/Venda.java
package com.titanaxis.model;

import java.time.LocalDateTime;
import java.util.List;

public class Venda {
    private int id;
    private int clienteId;
    private int usuarioId;
    private LocalDateTime dataVenda;
    private double valorTotal;
    private List<VendaItem> itens;
    private String nomeCliente; // Para exibição em relatórios/tabelas

    // Construtor para novas vendas
    public Venda(int clienteId, int usuarioId, double valorTotal, List<VendaItem> itens) {
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.valorTotal = valorTotal;
        this.itens = itens;
    }

    public Venda(int id, int clienteId, int usuarioId, LocalDateTime dataVenda, double valorTotal, String nomeCliente) {
        this.id = id;
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.dataVenda = dataVenda;
        this.valorTotal = valorTotal;
        this.nomeCliente = nomeCliente;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public List<VendaItem> getItens() { return itens; }
    public void setItens(List<VendaItem> itens) { this.itens = itens; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
}