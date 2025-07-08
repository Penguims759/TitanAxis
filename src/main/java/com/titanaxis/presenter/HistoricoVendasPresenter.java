// src/main/java/com/titanaxis/presenter/HistoricoVendasPresenter.java
package com.titanaxis.presenter;

import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Venda;
import com.titanaxis.service.VendaService;
import com.titanaxis.view.interfaces.HistoricoVendasView;
import javax.swing.SwingWorker;
import java.util.List;

public class HistoricoVendasPresenter implements HistoricoVendasView.HistoricoVendasListener {

    private final HistoricoVendasView view;
    private final VendaService vendaService;

    public HistoricoVendasPresenter(HistoricoVendasView view, VendaService vendaService) {
        this.view = view;
        this.vendaService = vendaService;
        this.view.setListener(this);
    }

    @Override
    public void aoAplicarFiltros() {
        view.setCarregando(true);
        SwingWorker<List<Venda>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Venda> doInBackground() throws Exception {
                return vendaService.buscarVendasPorFiltro(
                        view.getDataInicio().orElse(null),
                        view.getDataFim().orElse(null),
                        view.getStatusFiltro().orElse(null),
                        view.getClienteNomeFiltro()
                );
            }

            @Override
            protected void done() {
                try {
                    List<Venda> vendas = get();
                    view.setVendasNaTabela(vendas);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String msg = (cause instanceof PersistenciaException) ? "Erro de Base de Dados: " + cause.getMessage() : "Ocorreu um erro inesperado.";
                    view.mostrarErro("Erro ao Carregar Vendas", msg);
                } finally {
                    view.setCarregando(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void aoLimparFiltros() {
        // A lógica de limpar os componentes da UI está no próprio painel.
        // Após limpar, o painel deve chamar aoAplicarFiltros().
        aoAplicarFiltros();
    }

    @Override
    public void aoVerDetalhesVenda(int vendaId) {
        // A lógica para mostrar o diálogo de detalhes será tratada pelo próprio painel.
    }
}