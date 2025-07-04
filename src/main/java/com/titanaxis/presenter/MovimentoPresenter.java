package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.service.MovimentoService;
import com.titanaxis.view.interfaces.MovimentoView;

import javax.swing.*;
import java.util.List;

public class MovimentoPresenter implements MovimentoView.MovimentoViewListener {
    private final MovimentoView view; // Adicionado final
    private final MovimentoService movimentoService; // Adicionado final

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
                    // Trata a PersistenciaException que pode vir do SwingWorker
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof PersistenciaException) {
                        // ALTERADO: Mensagem mais informativa
                        view.mostrarErro("Erro de Base de Dados", "Erro ao carregar o histórico de movimentos: " + cause.getMessage());
                    } else {
                        view.mostrarErro("Erro Inesperado", "Ocorreu um erro inesperado: " + cause.getMessage()); // ALTERADO: Mensagem mais informativa
                    }
                    cause.printStackTrace(); // Manter para depuração completa nos logs
                } finally {
                    view.setCursorEspera(false);
                }
            }
        };
        worker.execute();
    }
}