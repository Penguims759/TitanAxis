package com.titanaxis.service;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.repository.impl.AuditoriaRepositoryImpl;
import com.titanaxis.repository.impl.UsuarioRepositoryImpl;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.PasswordUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private Usuario usuarioLogado;
    private static final Logger logger = AppLogger.getLogger();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = new AuditoriaRepositoryImpl();
    }

    public AuthService() {
        this(new UsuarioRepositoryImpl());
    }

    public Optional<Usuario> login(String nomeUsuario, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNomeUsuario(nomeUsuario);
        if (usuarioOpt.isPresent() && PasswordUtil.checkPassword(senha, usuarioOpt.get().getSenhaHash())) {
            this.usuarioLogado = usuarioOpt.get();
            logger.info("Login bem-sucedido para: " + nomeUsuario);

            String detalhes = String.format("Login bem-sucedido para o usuário '%s'.", nomeUsuario);
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), nomeUsuario, "LOGIN_SUCESSO", "Autenticação", detalhes);
            return usuarioOpt;
        }

        logger.warning("Falha no login para: " + nomeUsuario);
        String detalhes = String.format("Tentativa de login falhada para o usuário '%s'.", nomeUsuario);
        Integer idTentativa = usuarioOpt.map(Usuario::getId).orElse(null);
        auditoriaRepository.registrarAcao(idTentativa, nomeUsuario, "LOGIN_FALHA", "Autenticação", detalhes);

        this.usuarioLogado = null;
        return Optional.empty();
    }

    public boolean cadastrarUsuario(String nomeUsuario, String senha, NivelAcesso nivelAcesso, Usuario ator) {
        if (usuarioRepository.findByNomeUsuario(nomeUsuario).isPresent()) {
            logger.warning("Tentativa de cadastrar usuário existente: " + nomeUsuario);
            return false;
        }
        String senhaHash = PasswordUtil.hashPassword(senha);
        Usuario novoUsuario = new Usuario(nomeUsuario, senhaHash, nivelAcesso);
        return usuarioRepository.save(novoUsuario, ator) != null;
    }

    public Optional<Usuario> getUsuarioLogado() {
        return Optional.ofNullable(usuarioLogado);
    }

    public int getUsuarioLogadoId() {
        return usuarioLogado != null ? usuarioLogado.getId() : -1;
    }

    public boolean isAdmin() {
        return usuarioLogado != null && usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN;
    }

    public boolean isGerente() {
        return usuarioLogado != null && (usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN || usuarioLogado.getNivelAcesso() == NivelAcesso.GERENTE);
    }

    public void logout() {
        if (usuarioLogado != null) {
            logger.info("Logout do usuário: " + usuarioLogado.getNomeUsuario());
        }
        this.usuarioLogado = null;
    }

    public List<Usuario> listarUsuarios() {
        try {
            return usuarioRepository.findAll();
        } catch (Exception e) {
            logger.severe("Erro ao listar usuários: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Usuario> buscarUsuariosPorNomeContendo(String termo) {
        try {
            return usuarioRepository.findByNomeContaining(termo);
        } catch (Exception e) {
            logger.severe("Erro ao buscar usuários: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean atualizarUsuario(Usuario usuario, Usuario ator) {
        return usuarioRepository.save(usuario, ator) != null;
    }

    public void deletarUsuario(int id, Usuario ator) {
        usuarioRepository.deleteById(id, ator);
    }
}