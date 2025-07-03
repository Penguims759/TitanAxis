// penguims759/titanaxis/Penguims759-TitanAxis-7ba36152a6e3502010a8be48ce02c9ed9fcd8bf0/src/main/java/com/titanaxis/service/VendaService.java
package com.titanaxis.service;

import com.titanaxis.model.Usuario;
import com.titanaxis.model.Venda;
import com.titanaxis.repository.VendaRepository;
import com.titanaxis.repository.impl.VendaRepositoryImpl;

public class VendaService {

    private final VendaRepository vendaRepository;

    public VendaService(VendaRepository vendaRepository) {
        this.vendaRepository = vendaRepository;
    }

    public Venda finalizarVenda(Venda venda, Usuario ator) throws Exception {
        if (ator == null) {
            throw new Exception("Nenhum utilizador autenticado para realizar a venda.");
        }
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new Exception("A venda não contém itens.");
        }

        // A lógica de salvar a venda e os itens, assim como a auditoria,
        // será tratada na camada de repositório.
        return vendaRepository.save(venda, ator);
    }
}