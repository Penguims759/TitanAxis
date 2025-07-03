package com.titanaxis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nome_usuario", nullable = false, unique = true)
    private String nomeUsuario;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    // A anotação @Enumerated foi removida. O nosso NivelAcessoConverter fará o trabalho.
    @Column(name = "nivel_acesso", nullable = false)
    private NivelAcesso nivelAcesso;

    public Usuario() {
    }

    public Usuario(int id, String nomeUsuario, String senhaHash, NivelAcesso nivelAcesso) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
        this.nivelAcesso = nivelAcesso;
    }

    public Usuario(String nomeUsuario, String senhaHash, NivelAcesso nivelAcesso) {
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
        this.nivelAcesso = nivelAcesso;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getSenhaHash() { return senhaHash; }
    public NivelAcesso getNivelAcesso() { return nivelAcesso; }

    public void setId(int id) { this.id = id; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setNivelAcesso(NivelAcesso nivelAcesso) { this.nivelAcesso = nivelAcesso; }
}