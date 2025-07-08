// src/main/java/com/titanaxis/view/interfaces/HistoricoVendasView.java
package com.titanaxis.view.interfaces;

import com.titanaxis.model.Venda;
import com.titanaxis.model.VendaStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoricoVendasView {
    void setVendasNaTabela(List<Venda> vendas);
    void mostrarErro(String titulo, String mensagem);
    void setCarregando(boolean carregando);

    Optional<LocalDate> getDataInicio();
    Optional<LocalDate> getDataFim();
    Optional<VendaStatus> getStatusFiltro();
    String getClienteNomeFiltro();

    interface HistoricoVendasListener {
        void aoAplicarFiltros();
        void aoLimparFiltros();
        void aoVerDetalhesVenda(int vendaId);
    }

    void setListener(HistoricoVendasListener listener);
}