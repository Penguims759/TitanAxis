package com.titanaxis.view.interfaces;

import com.titanaxis.model.MovimentoEstoque;
import java.util.List;

public interface MovimentoView {
    void setMovimentosNaTabela(List<MovimentoEstoque> movimentos);
    void mostrarErro(String titulo, String mensagem);
    void setCursorEspera(boolean emEspera);

    interface MovimentoViewListener {
        void aoCarregarMovimentos();
    }

    void setListener(MovimentoViewListener listener);
}