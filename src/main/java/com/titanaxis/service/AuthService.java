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

    public Optional<Usuario> login(String nomeUsuario, String senha) {
        // A operação de login é uma consulta, então também é envolvida numa transação de leitura.
        Optional<Usuario> usuarioOpt = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findByNomeUsuario(nomeUsuario, em)
        );

        if (usuarioOpt.isPresent() && PasswordUtil.checkPassword(senha, usuarioOpt.get().getSenhaHash())) {
            this.usuarioLogado = usuarioOpt.get();
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), nomeUsuario, "LOGIN_SUCESSO", "Autenticação", "Login bem-sucedido.");
            return usuarioOpt;
        }

        Integer idTentativa = usuarioOpt.map(Usuario::getId).orElse(null);
        auditoriaRepository.registrarAcao(idTentativa, nomeUsuario, "LOGIN_FALHA", "Autenticação", "Tentativa de login falhada.");
        this.usuarioLogado = null;
        return Optional.empty();
    }

    public boolean cadastrarUsuario(String nomeUsuario, String senha, NivelAcesso nivelAcesso, Usuario ator) {
        return transactionService.executeInTransactionWithResult(em -> {
            if (usuarioRepository.findByNomeUsuario(nomeUsuario, em).isPresent()) {
                // Lançar exceção dentro da transação para garantir o rollback
                throw new RuntimeException("O nome de utilizador já existe.");
            }
            String senhaHash = PasswordUtil.hashPassword(senha);
            Usuario novoUsuario = new Usuario(nomeUsuario, senhaHash, nivelAcesso);
            return usuarioRepository.save(novoUsuario, ator, em) != null;
        });
    }

    public boolean atualizarUsuario(Usuario usuario, Usuario ator) {
        Usuario salvo = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.save(usuario, ator, em)
        );
        return salvo != null;
    }

    public void deletarUsuario(int id, Usuario ator) {
        transactionService.executeInTransaction(em ->
                usuarioRepository.deleteById(id, ator, em)
        );
    }

    public List<Usuario> listarUsuarios() {
        return transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findAll(em)
        );
    }

    public List<Usuario> buscarUsuariosPorNomeContendo(String termo) {
        return transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findByNomeContaining(termo, em)
        );
    }

    public Optional<Usuario> getUsuarioLogado() { return Optional.ofNullable(usuarioLogado); }
    public int getUsuarioLogadoId() { return usuarioLogado != null ? usuarioLogado.getId() : 0; }
    public boolean isAdmin() { return usuarioLogado != null && usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN; }
    public boolean isGerente() { return usuarioLogado != null && (usuarioLogado.getNivelAcesso() == NivelAcesso.ADMIN || usuarioLogado.getNivelAcesso() == NivelAcesso.GERENTE); }
    public void logout() { this.usuarioLogado = null; }
}