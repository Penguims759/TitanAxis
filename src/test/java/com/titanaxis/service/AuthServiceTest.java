package com.titanaxis.service;

import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.UsuarioRepository;
import com.titanaxis.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_deveSerBemSucedido_quandoUsuarioESenhaCorretos() {
        // Arrange
        String username = "teste";
        String password = "senha_correta";
        String hashedPassword = PasswordUtil.hashPassword(password);
        Usuario usuarioDoBanco = new Usuario(1, username, hashedPassword, NivelAcesso.PADRAO);

        when(usuarioRepository.findByNomeUsuario(username)).thenReturn(Optional.of(usuarioDoBanco));

        // Act
        Optional<Usuario> resultado = authService.login(username, password);

        // Assert
        assertTrue(resultado.isPresent(), "O login deveria ter sido bem-sucedido.");
        assertEquals(username, resultado.get().getNomeUsuario());
        verify(auditoriaRepository).registrarAcao(eq(1), eq(username), eq("LOGIN_SUCESSO"), anyString(), anyString());
    }

    @Test
    void login_deveFalhar_quandoSenhaIncorreta() {
        // Arrange
        String username = "teste";
        String passwordCorreta = "senha_correta";
        String passwordIncorreta = "senha_errada";
        String hashedPassword = PasswordUtil.hashPassword(passwordCorreta);
        Usuario usuarioDoBanco = new Usuario(1, username, hashedPassword, NivelAcesso.PADRAO);

        when(usuarioRepository.findByNomeUsuario(username)).thenReturn(Optional.of(usuarioDoBanco));

        // Act
        Optional<Usuario> resultado = authService.login(username, passwordIncorreta);

        // Assert
        assertFalse(resultado.isPresent(), "O login deveria ter falhado.");
        verify(auditoriaRepository).registrarAcao(eq(1), eq(username), eq("LOGIN_FALHA"), anyString(), anyString());
    }

    @Test
    void login_deveFalhar_quandoUsuarioNaoExiste() {
        // Arrange
        String username = "nao_existe";
        when(usuarioRepository.findByNomeUsuario(username)).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = authService.login(username, "qualquer_senha");

        // Assert
        assertFalse(resultado.isPresent());
        verify(auditoriaRepository).registrarAcao(isNull(), eq(username), eq("LOGIN_FALHA"), anyString(), anyString());
    }
}