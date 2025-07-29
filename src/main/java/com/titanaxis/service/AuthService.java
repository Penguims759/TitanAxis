package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.I18n; // Importado
import com.titanaxis.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final TransactionService transactionService;
    private Usuario usuarioLogado;

    @Inject
    public AuthService(UsuarioRepository usuarioRepository, AuditoriaRepository auditoriaRepository, TransactionService transactionService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.transactionService = transactionService;
    }

    public Optional<Usuario> login(String nomeUsuario, String senha) throws PersistenciaException {
        Optional<Usuario> usuarioOpt = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.findByNomeUsuario(nomeUsuario, em)
        );

        if (usuarioOpt.isPresent() && PasswordUtil.checkPassword(senha, usuarioOpt.get().getSenhaHash())) {
            this.usuarioLogado = usuarioOpt.get();
            transactionService.executeInTransaction(em ->
                    auditoriaRepository.registrarAcao(usuarioLogado.getId(), nomeUsuario, "LOGIN_SUCESSO", "Autenticação", I18n.getString("service.auth.log.loginSuccess"), em) 
            );
            return Optional.of(usuarioLogado);
        }

        
        String detalhes = usuarioOpt.isPresent() ? I18n.getString("service.auth.log.loginFail.wrongPassword") : I18n.getString("service.auth.log.loginFail.userNotFound");
        Integer idTentativa = usuarioOpt.map(Usuario::getId).orElse(null);
        transactionService.executeInTransaction(em ->
                auditoriaRepository.registrarAcao(idTentativa, nomeUsuario, "LOGIN_FALHA", "Autenticação", detalhes, em)
        );
        this.usuarioLogado = null;
        return Optional.empty();
    }

    public void cadastrarUsuario(String nomeUsuario, String senha, NivelAcesso nivelAcesso, Usuario ator) throws NomeDuplicadoException, UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); 
        }
        try {
            transactionService.executeInTransaction(em -> {
                if (usuarioRepository.findByNomeUsuario(nomeUsuario, em).isPresent()) {
                    throw new RuntimeException(I18n.getString("service.auth.error.userExists", nomeUsuario)); 
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
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); 
        }
        Usuario salvo = transactionService.executeInTransactionWithResult(em ->
                usuarioRepository.save(usuario, ator, em)
        );
        return salvo != null;
    }

    public void deletarUsuario(int id, Usuario ator) throws UtilizadorNaoAutenticadoException, PersistenciaException {
        if (ator == null) {
            throw new UtilizadorNaoAutenticadoException(I18n.getString("service.auth.error.notAuthenticated")); 
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