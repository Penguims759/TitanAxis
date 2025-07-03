// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/service/CategoriaService.java
package com.titanaxis.service;

import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.repository.CategoriaRepository;
import com.titanaxis.repository.impl.CategoriaRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodasCategorias() {
        return categoriaRepository.findAllWithProductCount();
    }

    public List<Categoria> buscarCategoriasPorNome(String termo) {
        return categoriaRepository.findByNomeContainingWithProductCount(termo);
    }

    public void salvar(Categoria categoria, Usuario usuarioLogado) throws Exception {
        // Validação de nome duplicado
        Optional<Categoria> catExistenteOpt = categoriaRepository.findByNome(categoria.getNome());
        if (catExistenteOpt.isPresent() && catExistenteOpt.get().getId() != categoria.getId()) {
            throw new Exception("Já existe uma categoria com este nome.");
        }

        if (usuarioLogado == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar esta operação.");
        }

        categoriaRepository.save(categoria, usuarioLogado);
    }

    public void deletar(int id, Usuario usuarioLogado) {
        if (usuarioLogado != null) {
            categoriaRepository.deleteById(id, usuarioLogado);
        }
        // Opcional: Lançar uma exceção se o utilizador for nulo
    }
}