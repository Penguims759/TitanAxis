package com.titanaxis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contas_a_receber")
public class ContasAReceber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Column(name = "numero_parcela", nullable = false)
    private int numeroParcela;

    @Column(name = "valor_parcela", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double valorParcela;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(nullable = false, length = 50)
    private String status; // Ex: "Pendente", "Pago", "Atrasado"

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public int getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(int numeroParcela) { this.numeroParcela = numeroParcela; }
    public double getValorParcela() { return valorParcela; }
    public void setValorParcela(double valorParcela) { this.valorParcela = valorParcela; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}