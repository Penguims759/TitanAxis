package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.MovimentoEstoque;
import com.titanaxis.service.MovimentoService;
import com.titanaxis.util.I18n;
import com.titanaxis.view.interfaces.MovimentoView;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;

public class MovimentoPresenter implements MovimentoView.MovimentoViewListener {
    private final MovimentoView view;
    private final MovimentoService movimentoService;

    public MovimentoPresenter(MovimentoView view, MovimentoService movimentoService) {
        this.view = view;
        this.movimentoService = movimentoService;
        this.view.setListener(this);
        aoCarregarMovimentos();
    }

    @Override
    public void aoCarregarMovimentos() {
        carregarMovimentos(null, null);
    }

    @Override
    public void aoFiltrarPorData() {
        carregarMovimentos(view.getDataInicio(), view.getDataFim());
    }

    private void carregarMovimentos(LocalDate inicio, LocalDate fim) {
        view.setCursorEspera(true);
        SwingWorker<List<MovimentoEstoque>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MovimentoEstoque> doInBackground() throws Exception {
                if (inicio != null && fim != null) {
                    return movimentoService.listarMovimentosPorPeriodo(inicio, fim);
                }
                return movimentoService.listarTodosMovimentos();
            }

            @Override
            protected void done() {
                try {
                    List<MovimentoEstoque> movimentos = get();
                    view.setMovimentosNaTabela(movimentos);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String msg = (cause instanceof PersistenciaException) ? I18n.getString("error.db.generic", cause.getMessage()) : I18n.getString("error.unexpected.message");
                    view.mostrarErro(I18n.getString("presenter.movement.error.load.title"), msg);
                } finally {
                    view.setCursorEspera(false);
                }
            }
        };
        worker.execute();
    }
}