// src/main/java/com/titanaxis/model/Usuario.java
package com.titanaxis.model; // ALTERADO

/**
 * Representa a entidade Usuário no sistema.
 * Contém informações como ID, nome de usuário, hash da senha e nível de acesso.
 */
public class Usuario {
    private int id;
    private String nomeUsuario;
    private String senhaHash;
    private String nivelAcesso;

    public Usuario(int id, String nomeUsuario, String senhaHash, String nivelAcesso) {
        this.id = id;
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
        this.nivelAcesso = nivelAcesso;
    }

    public Usuario(String nomeUsuario, String senhaHash, String nivelAcesso) {
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
        this.nivelAcesso = nivelAcesso;
    }

    public int getId() { return id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getSenhaHash() { return senhaHash; }
    public String getNivelAcesso() { return nivelAcesso; }

    public void setId(int id) { this.id = id; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setNivelAcesso(String nivelAcesso) { this.nivelAcesso = nivelAcesso; }
}