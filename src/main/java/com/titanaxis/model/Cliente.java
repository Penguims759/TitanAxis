// Exemplo: model/Cliente.java
package com.titanaxis.model;

public class Cliente {
    private int id;
    private String nome;
    private String contato; // Ex: email, telefone
    private String endereco;

    public Cliente(int id, String nome, String contato, String endereco) {
        this.id = id;
        this.nome = nome;
        this.contato = contato;
        this.endereco = endereco;
    }
    // Construtor para novo cliente
    public Cliente(String nome, String contato, String endereco) {
        this.nome = nome;
        this.contato = contato;
        this.endereco = endereco;
    }
    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getContato() { return contato; }
    public String getEndereco() { return endereco; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setContato(String contato) { this.contato = contato; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}