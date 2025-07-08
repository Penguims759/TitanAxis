// src/main/java/com/titanaxis/model/Fornecedor.java
package com.titanaxis.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "fornecedores")
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(name = "contato_nome")
    private String contatoNome;

    @Column(name = "contato_telefone")
    private String contatoTelefone;

    @Column(name = "contato_email")
    private String contatoEmail;

    private String cnpj;
    private String endereco;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getContatoNome() { return contatoNome; }
    public void setContatoNome(String contatoNome) { this.contatoNome = contatoNome; }
    public String getContatoTelefone() { return contatoTelefone; }
    public void setContatoTelefone(String contatoTelefone) { this.contatoTelefone = contatoTelefone; }
    public String getContatoEmail() { return contatoEmail; }
    public void setContatoEmail(String contatoEmail) { this.contatoEmail = contatoEmail; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        return this.nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fornecedor that = (Fornecedor) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}