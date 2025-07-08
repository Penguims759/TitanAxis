package com.titanaxis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nome;

    private String contato; // Ex: email, telefone

    private String endereco;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double credito; // NOVO

    // Construtor vazio para o JPA
    public Cliente() {
        this.credito = 0.0;
    }

    public Cliente(int id, String nome, String contato, String endereco) {
        this.id = id;
        this.nome = nome;
        this.contato = contato;
        this.endereco = endereco;
        this.credito = 0.0;
    }

    // Construtor para novo cliente
    public Cliente(String nome, String contato, String endereco) {
        this.nome = nome;
        this.contato = contato;
        this.endereco = endereco;
        this.credito = 0.0;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getContato() { return contato; }
    public String getEndereco() { return endereco; }
    public double getCredito() { return credito; } // NOVO

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setContato(String contato) { this.contato = contato; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setCredito(double credito) { this.credito = credito; } // NOVO
    public void adicionarCredito(double valor) { this.credito += valor; } // NOVO

    @Override
    public String toString() {
        return this.nome; // Para exibição em JComboBoxes, etc.
    }
}