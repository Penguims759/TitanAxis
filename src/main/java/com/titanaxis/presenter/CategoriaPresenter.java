package com.titanaxis.presenter;

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
        carregarDadosIniciais();
    }

    private void carregarDadosIniciais() {
        List<Categoria> categorias = categoriaService.listarTodasCategorias();
        view.setCategoriasNaTabela(categorias);
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
            carregarDadosIniciais();
        } catch (Exception e) {
            // A exceção de nome duplicado vinda do serviço será exibida aqui.
            view.mostrarMensagem("Erro", "Erro ao salvar categoria: " + e.getMessage(), true);
        }
    }

    @Override
    public void aoApagar() {
        if (view.getId().isEmpty()) {
            view.mostrarMensagem("Aviso", "Selecione uma categoria para eliminar.", true);
            return;
        }

        String mensagemConfirmacao = "Tem certeza que deseja eliminar esta categoria?\n(Os produtos nesta categoria ficarão sem categoria definida)";
        if (!view.mostrarConfirmacao("Confirmar Eliminação", mensagemConfirmacao)) {
            return;
        }

        int id = Integer.parseInt(view.getId());
        Usuario ator = authService.getUsuarioLogado().orElse(null);
        try {
            categoriaService.deletar(id, ator);
            view.mostrarMensagem("Sucesso", "Categoria eliminada com sucesso!", false);
            aoLimpar();
            carregarDadosIniciais();
        } catch (Exception e) {
            view.mostrarMensagem("Erro", "Erro ao eliminar categoria: " + e.getMessage(), true);
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
        String termo = view.getTermoBusca();
        if (termo != null && !termo.trim().isEmpty()) {
            view.setCategoriasNaTabela(categoriaService.buscarCategoriasPorNome(termo));
        } else {
            carregarDadosIniciais();
        }
    }

    @Override
    public void aoLimparBusca() {
        view.setTermoBusca("");
        carregarDadosIniciais();
    }

    @Override
    public void aoSelecionarCategoria(Categoria categoria) {
        if (categoria != null) {
            view.setId(String.valueOf(categoria.getId()));
            view.setNome(categoria.getNome());
        }
    }
}