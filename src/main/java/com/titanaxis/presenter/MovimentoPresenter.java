package com.titanaxis.presenter;

import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.service.MovimentoService;
import com.titanaxis.view.interfaces.MovimentoView;

import javax.swing.*;
import java.util.List;

public class MovimentoPresenter implements MovimentoView.MovimentoViewListener {
    private final MovimentoView view;
    private final MovimentoService movimentoService;

    public MovimentoPresenter(MovimentoView view, MovimentoService movimentoService) {
        this.view = view;
        this.movimentoService = movimentoService;
        this.view.setListener(this);
    }

    @Override
    public void aoCarregarMovimentos() {
        view.setCursorEspera(true);
        SwingWorker<List<MovimentoEstoque>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MovimentoEstoque> doInBackground() throws Exception {
                return movimentoService.listarTodosMovimentos();
            }

            @Override
            protected void done() {
                try {
                    List<MovimentoEstoque> movimentos = get();
                    view.setMovimentosNaTabela(movimentos);
                } catch (Exception e) {
                    view.mostrarErro("Erro de Base de Dados", "Erro ao carregar o histórico de movimentos.");
                    e.printStackTrace();
                } finally {
                    view.setCursorEspera(false);
                }
            }
        };
        worker.execute();
    }
}