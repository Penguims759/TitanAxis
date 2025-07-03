package com.titanaxis.model;

import jakarta.persistence.*;

/**
 * Representa a entidade Usuário no sistema.
 * Contém informações como ID, nome de usuário, hash da senha e nível de acesso.
 */
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

    // CORREÇÃO: Removemos a anotação @Enumerated.
    // O @Converter(autoApply = true) na classe NivelAcessoConverter fará o trabalho.
    @Column(name = "nivel_acesso", nullable = false)
    private NivelAcesso nivelAcesso;

    // Construtor vazio exigido pelo JPA
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

    // Getters e Setters (sem alterações)
    public int getId() { return id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getSenhaHash() { return senhaHash; }
    public NivelAcesso getNivelAcesso() { return nivelAcesso; }

    public void setId(int id) { this.id = id; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setNivelAcesso(NivelAcesso nivelAcesso) { this.nivelAcesso = nivelAcesso; }
}