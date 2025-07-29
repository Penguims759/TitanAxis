package com.titanaxis.service;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Test
    void login_success_returns_user_and_records_audit() throws PersistenciaException {
        UsuarioRepository userRepo = mock(UsuarioRepository.class);
        AuditoriaRepository auditRepo = mock(AuditoriaRepository.class);
        TransactionService txService = mock(TransactionService.class);
        AuthService service = new AuthService(userRepo, auditRepo, txService);

        EntityManager em = mock(EntityManager.class);
        String hash = PasswordUtil.hashPassword("secret");
        Usuario user = new Usuario(1, "admin", hash, NivelAcesso.ADMIN);

        when(txService.executeInTransactionWithResult(any())).thenAnswer(inv -> {
            Function<EntityManager, Optional<Usuario>> func = inv.getArgument(0);
            return func.apply(em);
        });
        doAnswer(inv -> { Consumer<EntityManager> c = inv.getArgument(0); c.accept(em); return null; })
                .when(txService).executeInTransaction(any());
        when(userRepo.findByNomeUsuario("admin", em)).thenReturn(Optional.of(user));

        Optional<Usuario> result = service.login("admin", "secret");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepo).findByNomeUsuario("admin", em);
        verify(auditRepo).registrarAcao(eq(user.getId()), eq("admin"), eq("LOGIN_SUCESSO"), eq("Autenticação"), anyString(), eq(em));
    }
}
