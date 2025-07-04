package com.titanaxis.service;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
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

    // ALTERADO: Adicionada a declaração "throws PersistenciaException"
    public Optional<Usuario> login(String nomeUsuario, String senha) throws PersistenciaException {
        Optional<Usuario> usuarioOpt = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findByNomeUsuario(nomeUsuario, em)
        );

        if (usuarioOpt.isPresent() && PasswordUtil.checkPassword(senha, usuarioOpt.get().getSenhaHash())) {
            this.usuarioLogado = usuarioOpt.get();
            auditoriaRepository.registrarAcao(usuarioLogado.getId(), nomeUsuario, "LOGIN_SUCESSO", "Autenticação", "Login bem-sucedido.");
            return Optional.of(usuarioLogado);
        }

        String detalhes = usuarioOpt.isPresent() ? "Tentativa de login com senha incorreta." : "Tentativa de login com utilizador inexistente.";
        Integer idTentativa = usuarioOpt.map(Usuario::getId).orElse(null);
        auditoriaRepository.registrarAcao(idTentativa, nomeUsuario, "LOGIN_FALHA", "Autenticação", detalhes);
        this.usuarioLogado = null;
        return Optional.empty();
    }

    public void cadastrarUsuario(String nomeUsuario, String senha, NivelAcesso nivelAcesso, Usuario ator) throws NomeDuplicadoException, UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        try {
            transactionService.executeInTransaction(em -> {
                if (usuarioRepository.findByNomeUsuario(nomeUsuario, em).isPresent()) {
                    throw new RuntimeException("O nome de utilizador '" + nomeUsuario + "' já existe.");
                }
                String senhaHash = PasswordUtil.hashPassword(senha);
                Usuario novoUsuario = new Usuario(nomeUsuario, senhaHash, nivelAcesso);
                usuarioRepository.save(novoUsuario, ator, em);
            });
        } catch (RuntimeException e) {
            throw new NomeDuplicadoException(e.getMessage());
        }
    }

    public boolean atualizarUsuario(Usuario usuario, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        Usuario salvo = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.save(usuario, ator, em)
        );
        return salvo != null;
    }

    public void deletarUsuario(int id, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException("Nenhum utilizador autenticado para realizar esta operação.");
        }
        transactionService.executeInTransaction(em ->
                usuarioRepository.deleteById(id, ator, em)
        );
    }

    public List<Usuario> listarUsuarios() throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findAll(em)
        );
    }

    public List<Usuario> buscarUsuariosPorNomeContendo(String termo) throws PersistenciaException {
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