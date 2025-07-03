package com.titanaxis.service;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.NivelAcesso;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void salvar_deveChamarRepositorio_quandoNomeNaoExiste() throws Exception {
        // --- Arrange ---
        Categoria novaCategoria = new Categoria("Eletrónicos");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(categoriaRepository.findByNome("Eletrónicos")).thenReturn(Optional.empty());

        // --- Act ---
        categoriaService.salvar(novaCategoria, ator);

        // --- Assert ---
        verify(categoriaRepository, times(1)).save(novaCategoria, ator);
    }

    @Test
    void salvar_deveLancarExcecao_quandoNomeJaExisteParaOutraCategoria() {
        // --- Arrange ---
        Categoria categoriaAserSalva = new Categoria("Livros");
        Categoria categoriaExistente = new Categoria(5, "Livros");
        when(categoriaRepository.findByNome("Livros")).thenReturn(Optional.of(categoriaExistente));
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);

        // --- Act & Assert ---
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.salvar(categoriaAserSalva, ator);
        });

        assertEquals("Já existe uma categoria com este nome.", exception.getMessage());
        verify(categoriaRepository, never()).save(any(), any());
    }

    @Test
    void salvar_devePermitirAtualizar_quandoNomeNaoMuda() throws Exception {
        // --- Arrange ---
        Categoria categoriaParaAtualizar = new Categoria(10, "Roupas");
        Usuario ator = new Usuario(1, "admin", "hash", NivelAcesso.ADMIN);
        when(categoriaRepository.findByNome("Roupas")).thenReturn(Optional.of(categoriaParaAtualizar));

        // --- Act ---
        categoriaService.salvar(categoriaParaAtualizar, ator);

        // --- Assert ---
        verify(categoriaRepository, times(1)).save(categoriaParaAtualizar, ator);
    }
}