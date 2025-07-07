package com.titanaxis.view.interfaces;

import com.titanaxis.model.MovimentoEstoque;
import java.time.LocalDate;
import java.util.List;

public interface MovimentoView {
    void setMovimentosNaTabela(List<MovimentoEstoque> movimentos);
    void mostrarErro(String titulo, String mensagem);
    void setCursorEspera(boolean emEspera);
    LocalDate getDataInicio();
    LocalDate getDataFim();

    interface MovimentoViewListener {
        void aoCarregarMovimentos();
        void aoFiltrarPorData();
    }

    void setListener(MovimentoViewListener listener);
}