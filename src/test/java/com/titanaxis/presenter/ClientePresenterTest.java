package com.titanaxis.presenter;

import com.titanaxis.model.Cliente;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.ClienteService;
import com.titanaxis.view.interfaces.ClienteView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientePresenterTest {

    @Mock
    private ClienteView view;
    @Mock
    private ClienteService clienteService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private ClientePresenter presenter;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(authService.getUsuarioLogado()).thenReturn(Optional.of(admin));
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());
        presenter = new ClientePresenter(view, clienteService, authService);
    }

    @Test
    void aoSalvar_deveMostrarErro_seNomeForVazio() throws Exception {
        when(view.getNome()).thenReturn("   ");
        presenter.aoSalvar();
        verify(clienteService, never()).salvar(any(), any());
        verify(view).mostrarMensagem("Erro de Validação", "O nome do cliente é obrigatório.", true);
    }

    @Test
    void aoSalvar_deveCriarNovoCliente_seIdForVazio() throws Exception {
        when(view.getId()).thenReturn("");
        when(view.getNome()).thenReturn("Cliente Teste");
        when(view.getContato()).thenReturn("");
        when(view.getEndereco()).thenReturn("");

        presenter.aoSalvar();

        verify(clienteService).salvar(any(Cliente.class), any(Usuario.class));
        verify(view).mostrarMensagem(eq("Sucesso"), contains("adicionado"), eq(false));
    }

    @Test
    void aoSalvar_deveAtualizarCliente_seIdEstiverPreenchido() throws Exception {
        when(view.getId()).thenReturn("123");
        when(view.getNome()).thenReturn("Nome Atualizado");
        when(view.getContato()).thenReturn("Contato Att");
        when(view.getEndereco()).thenReturn("Endereco Att");

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        presenter.aoSalvar();
        verify(clienteService).salvar(clienteCaptor.capture(), any(Usuario.class));

        Cliente clienteAtualizado = clienteCaptor.getValue();
        assertEquals(123, clienteAtualizado.getId());
        assertEquals("Nome Atualizado", clienteAtualizado.getNome());
        verify(view).mostrarMensagem(eq("Sucesso"), contains("atualizado"), eq(false));
    }

    @Test
    void aoApagar_deveApagarCliente_quandoConfirmado() throws Exception {
        when(view.getId()).thenReturn("1");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(true);
        presenter.aoApagar();
        verify(clienteService).deletar(eq(1), any(Usuario.class));
        verify(view).mostrarMensagem("Sucesso", "Cliente eliminado com sucesso!", false);
    }

    @Test
    void aoApagar_naoDeveFazerNada_quandoNaoConfirmado() throws Exception {
        when(view.getId()).thenReturn("1");
        when(view.mostrarConfirmacao(anyString(), anyString())).thenReturn(false);
        presenter.aoApagar();
        verify(clienteService, never()).deletar(anyInt(), any(Usuario.class));
    }

    @Test
    void aoSelecionarCliente_devePreencherCamposDaView() {
        Cliente cliente = new Cliente(10, "Cliente Selecionado", "email@teste.com", "Rua X");
        presenter.aoSelecionarCliente(cliente);
        verify(view).setId("10");
        verify(view).setNome("Cliente Selecionado");
        verify(view).setContato("email@teste.com");
        verify(view).setEndereco("Rua X");
    }

    @Test
    void aoBuscar_deveChamarServico_eAtualizarTabela() {
        String termoBusca = "Teste";
        List<Cliente> resultado = List.of(new Cliente(1, "Cliente Teste", "", ""));
        when(view.getTermoBusca()).thenReturn(termoBusca);
        when(clienteService.buscarPorNome(termoBusca)).thenReturn(resultado);
        presenter.aoBuscar();
        verify(clienteService).buscarPorNome(termoBusca);
        verify(view).setClientesNaTabela(resultado);
    }
}