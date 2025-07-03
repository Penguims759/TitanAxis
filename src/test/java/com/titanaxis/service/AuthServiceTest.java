package com.titanaxis.service;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AuditoriaRepository auditoriaRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Simula o comportamento do serviço de transação para os testes
        doAnswer(invocation -> {
            Function<EntityManager, Object> action = invocation.getArgument(0);
            return action.apply(mock(EntityManager.class));
        }).when(transactionService).executeInTransactionWithResult(any());
    }


    @Test
    void login_deveSerBemSucedido_quandoUsuarioESenhaCorretos() {
        String username = "teste";
        String password = "senha_correta";
        String hashedPassword = PasswordUtil.hashPassword(password);
        Usuario usuarioDoBanco = new Usuario(1, username, hashedPassword, NivelAcesso.PADRAO);
        when(usuarioRepository.findByNomeUsuario(username)).thenReturn(Optional.of(usuarioDoBanco));

        Optional<Usuario> resultado = authService.login(username, password);

        assertTrue(resultado.isPresent());
        assertEquals(username, resultado.get().getNomeUsuario());
        verify(auditoriaRepository).registrarAcao(eq(1), eq(username), eq("LOGIN_SUCESSO"), anyString(), anyString());
    }

    @Test
    void login_deveFalhar_quandoSenhaIncorreta() {
        String username = "teste";
        String passwordCorreta = "senha_correta";
        String passwordIncorreta = "senha_errada";
        String hashedPassword = PasswordUtil.hashPassword(passwordCorreta);
        Usuario usuarioDoBanco = new Usuario(1, username, hashedPassword, NivelAcesso.PADRAO);
        when(usuarioRepository.findByNomeUsuario(username)).thenReturn(Optional.of(usuarioDoBanco));

        Optional<Usuario> resultado = authService.login(username, passwordIncorreta);

        assertFalse(resultado.isPresent());
        verify(auditoriaRepository).registrarAcao(eq(1), eq(username), eq("LOGIN_FALHA"), anyString(), anyString());
    }

    @Test
    void cadastrarUsuario_deveChamarRepositorioDentroDeUmaTransacao() {
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(usuarioRepository.findByNomeUsuario(anyString())).thenReturn(Optional.empty());

        authService.cadastrarUsuario("novo_user", "senha123", NivelAcesso.PADRAO, ator);

        verify(transactionService).executeInTransactionWithResult(any());
        verify(usuarioRepository).save(any(Usuario.class), eq(ator), any(EntityManager.class));
    }
}