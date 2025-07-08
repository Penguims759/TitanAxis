package com.titanaxis.presenter;

import com.titanaxis.exception.NomeDuplicadoException;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.exception.UtilizadorNaoAutenticadoException;
import com.titanaxis.model.Categoria;
import com.titanaxis.model.Usuario;
import com.titanaxis.service.AuthService;
import com.titanaxis.service.CategoriaService;
import com.titanaxis.view.interfaces.CategoriaView;

import java.util.List;

public class CategoriaPresenter implements CategoriaView.CategoriaViewListener {

    private final CategoriaView view;
    private final CategoriaService categoriaService;
    private final AuthService authService;

    public CategoriaPresenter(CategoriaView view, CategoriaService categoriaService, AuthService authService) {
        this.view = view;
        this.categoriaService = categoriaService;
        this.authService = authService;
        this.view.setListener(this);
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoCarregarDadosIniciais() {
        try {
            List<Categoria> categorias = categoriaService.listarTodasCategorias();
            view.setCategoriasNaTabela(categorias);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao carregar categorias: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoSalvar() {
        String nome = view.getNome().trim();
        if (nome.isEmpty()) {
            view.mostrarMensagem("Erro de Validação", "O nome da categoria é obrigatório.", true);
            return;
        }

        boolean isUpdate = !view.getId().isEmpty();
        int id = isUpdate ? Integer.parseInt(view.getId()) : 0;
        Categoria categoria = new Categoria(id, nome);
        Usuario ator = authService.getUsuarioLogado().orElse(null);

        try {
            categoriaService.salvar(categoria, ator);
            view.mostrarMensagem("Sucesso", "Categoria " + (isUpdate ? "atualizada" : "adicionada") + " com sucesso!", false);
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (NomeDuplicadoException e) {
            view.mostrarMensagem("Erro de Duplicação", e.getMessage(), true);
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem("Erro de Autenticação", e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Ocorreu um erro ao salvar a categoria: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem("Aviso", "Selecione uma categoria para eliminar.", true);
            return;
        }

        String nomeCategoria = view.getNome();
        String mensagem = String.format("Tem certeza que deseja eliminar a categoria '%s'?\n(Os produtos nesta categoria ficarão sem categoria definida)", nomeCategoria);

        if (!view.mostrarConfirmacao("Confirmar Eliminação", mensagem)) {
            return;
        }

        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            categoriaService.deletar(id, ator);
            view.mostrarMensagem("Sucesso", "Categoria eliminada com sucesso!", false);
            aoLimpar();
            aoCarregarDadosIniciais();
        } catch (UtilizadorNaoAutenticadoException e) {
            view.mostrarMensagem("Erro de Autenticação", e.getMessage(), true);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Ocorreu um erro ao eliminar a categoria: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoLimpar() {
        view.setId("");
        view.setNome("");
        view.clearTableSelection();
    }

    @Override
    public void aoBuscar() {
        try {
            String termo = view.getTermoBusca();
            if (termo != null && !termo.trim().isEmpty()) {
                view.setCategoriasNaTabela(categoriaService.buscarCategoriasPorNome(termo));
            } else {
                aoCarregarDadosIniciais();
            }
        }
        catch (PersistenciaException e) {
            view.mostrarMensagem("Erro de Base de Dados", "Falha ao buscar categorias: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        aoCarregarDadosIniciais();
    }

    @Override
    public void aoSelecionarCategoria(Categoria categoria) {
        if (categoria != null) {
            view.setId(String.valueOf(categoria.getId()));
            view.setNome(categoria.getNome());
        }
    }
}