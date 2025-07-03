package com.titanaxis.service;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final TransactionService transactionService;
    private Usuario usuarioLogado;

    public AuthService(UsuarioRepository usuarioRepository, AuditoriaRepository auditoriaRepository, TransactionService transactionService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.transactionService = transactionService;
    }

    /**
     * Lógica de login restaurada para usar a verificação de senha encriptada.
     */
    public Optional<Usuario> login(String nomeUsuario, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNomeUsuario(nomeUsuario);

        // A lógica original foi restaurada.
        if (usuarioOpt.isPresent() && PasswordUtil.checkPassword(senha, usuarioOpt.get().getSenhaHash())) {
            this.usuarioLogado = usuarioOpt.get();
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), nomeUsuario, "LOGIN_SUCESSO", "Autenticação", "Login bem-sucedido.");
            return usuarioOpt;
        }

        // Se o login falhar, regista o evento.
        Integer idTentativa = usuarioOpt.map(Usuario::getId).orElse(null);
        auditoriaRepository.registrarAcao(idTentativa, nomeUsuario, "LOGIN_FALHA", "Autenticação", "Tentativa de login falhada.");
        this.usuarioLogado = null;
        return Optional.empty();
    }

    // O resto da classe permanece como estava na última versão correta.
    public boolean cadastrarUsuario(String nomeUsuario, String senha, NivelAcesso nivelAcesso, Usuario ator) {
        if (usuarioRepository.findByNomeUsuario(nomeUsuario).isPresent()) {
            return false;
        }
        String senhaHash = PasswordUtil.hashPassword(senha);
        Usuario novoUsuario = new Usuario(nomeUsuario, senhaHash, nivelAcesso);

        Usuario salvo = transactionService.executeInTransactionWithResult(em -> {
            return usuarioRepository.save(novoUsuario, ator, em);
        });
        return salvo != null;
    }

    public boolean atualizarUsuario(Usuario usuario, Usuario ator) {
        Usuario salvo = transactionService.executeInTransactionWithResult(em -> {
            return usuarioRepository.save(usuario, ator, em);
        });
        return salvo != null;
    }

    public void deletarUsuario(int id, Usuario ator) {
        transactionService.executeInTransaction(em -> {
            usuarioRepository.deleteById(id, ator, em);
        });
    }

    public Optional<Usuario> getUsuarioLogado() { return Optional.ofNullable(usuarioLogado); }
    public int getUsuarioLogadoId() { return usuarioLogado != null ? usuarioLogado.getId() : 0; }
    public boolean isAdmin() { return usuarioLogado != null && usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN; }
    public boolean isGerente() { return usuarioLogado != null && (usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN || usuarioLogado.getNivelAcesso() == NivelAcesso.GERENTE); }
    public void logout() { this.usuarioLogado = null; }
    public List<Usuario> listarUsuarios() { return usuarioRepository.findAll(); }
    public List<Usuario> buscarUsuariosPorNomeContendo(String termo) { return usuarioRepository.findByNomeContaining(termo); }
}