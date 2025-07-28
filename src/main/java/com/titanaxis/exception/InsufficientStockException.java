package com.titanaxis.exception;

/**
 * Exceção lançada quando não há estoque suficiente para uma operação.
 */
public class InsufficientStockException extends BusinessException {
    private final int requestedQuantity;
    private final int availableQuantity;
    private final String productName;
    private final String lotNumber;

    public InsufficientStockException(String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Estoque insuficiente para o produto '%s'. Solicitado: %d, Disponível: %d", 
            productName, requestedQuantity, availableQuantity), "INSUFFICIENT_STOCK");
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.lotNumber = null;
    }

    public InsufficientStockException(String productName, String lotNumber, int requestedQuantity, int availableQuantity) {
        super(String.format("Estoque insuficiente para o produto '%s' do lote '%s'. Solicitado: %d, Disponível: %d", 
            productName, lotNumber, requestedQuantity, availableQuantity), "INSUFFICIENT_STOCK");
        this.productName = productName;
        this.lotNumber = lotNumber;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public int getShortfall() {
        return requestedQuantity - availableQuantity;
    }
}