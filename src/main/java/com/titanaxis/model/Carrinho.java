// src/main/java/com/titanaxis/model/Carrinho.java
package com.titanaxis.model;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private final Venda venda;
    private final List<VendaItem> itens;

    public Carrinho(Usuario vendedor) {
        this.venda = new Venda();
        this.venda.setUsuario(vendedor);
        this.itens = new ArrayList<>();
    }

    public void adicionarItem(Lote lote, int quantidade) throws IllegalArgumentException {
        if (lote == null) {
            throw new IllegalArgumentException("O lote não pode ser nulo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser positiva.");
        }
        if (quantidade > lote.getQuantidade()) {
            throw new IllegalArgumentException("Quantidade solicitada excede o estoque do lote (" + lote.getQuantidade() + ").");
        }

        // Verifica se o item já existe no carrinho para o mesmo lote e incrementa a quantidade
        for (VendaItem itemExistente : itens) {
            if (itemExistente.getLote().getId() == lote.getId()) {
                int novaQuantidade = itemExistente.getQuantidade() + quantidade;
                if (novaQuantidade > lote.getQuantidade()) {
                    throw new IllegalArgumentException("Quantidade total no carrinho (" + novaQuantidade + ") excede o estoque do lote.");
                }
                itemExistente.setQuantidade(novaQuantidade);
                recalcularTotal();
                return;
            }
        }

        // Se não existe, adiciona um novo item
        VendaItem novoItem = new VendaItem(lote, quantidade);
        this.itens.add(novoItem);
        recalcularTotal();
    }

    public void aplicarDescontoItem(int index, double desconto) {
        if (index >= 0 && index < this.itens.size()) {
            VendaItem item = this.itens.get(index);
            if (desconto >= 0 && desconto <= item.getPrecoUnitario() * item.getQuantidade()) {
                item.setDesconto(desconto);
                recalcularTotal();
            } else {
                throw new IllegalArgumentException("Desconto inválido.");
            }
        }
    }

    public void aplicarDescontoTotal(double desconto) {
        if (desconto >= 0) {
            this.venda.setDescontoTotal(desconto);
            recalcularTotal();
        } else {
            throw new IllegalArgumentException("Desconto total não pode ser negativo.");
        }
    }

    public void removerItem(int index) {
        if (index >= 0 && index < this.itens.size()) {
            this.itens.remove(index);
            recalcularTotal();
        }
    }

    public void limpar() {
        this.itens.clear();
        this.venda.setCliente(null);
        this.venda.setDescontoTotal(0);
        recalcularTotal();
    }

    public void recalcularTotal() {
        double subtotalItens = itens.stream()
                .mapToDouble(VendaItem::getSubtotal)
                .sum();
        double totalFinal = subtotalItens - this.venda.getDescontoTotal();
        this.venda.setValorTotal(Math.max(0, totalFinal)); // Garante que o total não seja negativo
    }

    public List<VendaItem> getItens() {
        return itens;
    }

    public Venda getVenda() {
        // Garante que a lista de itens na venda está sincronizada com a do carrinho
        this.venda.setItens(new ArrayList<>(this.itens));
        return venda;
    }

    public double getValorTotal() {
        return this.venda.getValorTotal();
    }

    public void setCliente(Cliente cliente) {
        this.venda.setCliente(cliente);
    }
}