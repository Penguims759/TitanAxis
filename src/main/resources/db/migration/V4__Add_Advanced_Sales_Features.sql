-- V4: Adiciona funcionalidades avançadas de vendas, como orçamentos, descontos, devoluções, etc.

-- Adiciona campos de desconto
ALTER TABLE vendas ADD COLUMN desconto_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE venda_itens ADD COLUMN desconto DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Adiciona campo para comissão do utilizador
ALTER TABLE usuarios ADD COLUMN percentual_comissao DECIMAL(5, 2) NOT NULL DEFAULT 0.00;

-- Adiciona a coluna para registrar o crédito utilizado na venda (NOVO)
ALTER TABLE vendas ADD COLUMN credito_utilizado DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Tabela para Devoluções
CREATE TABLE devolucoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    usuario_id INT NOT NULL,
    data_devolucao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT,
    valor_estornado DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES vendas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabela para Itens Devolvidos (liga a devolução aos itens específicos)
CREATE TABLE devolucao_itens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    devolucao_id INT NOT NULL,
    venda_item_id INT NOT NULL,
    quantidade_devolvida INT NOT NULL,
    FOREIGN KEY (devolucao_id) REFERENCES devolucoes(id) ON DELETE CASCADE,
    FOREIGN KEY (venda_item_id) REFERENCES venda_itens(id)
);

-- Tabela para Contas a Receber (vendas a prazo)
CREATE TABLE contas_a_receber (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    numero_parcela INT NOT NULL,
    valor_parcela DECIMAL(10, 2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    status VARCHAR(50) NOT NULL,
    UNIQUE(venda_id, numero_parcela),
    FOREIGN KEY (venda_id) REFERENCES vendas(id)
);

-- Tabela para Metas de Venda
CREATE TABLE metas_venda (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    ano_mes VARCHAR(7) NOT NULL, -- Formato 'AAAA-MM'
    valor_meta DECIMAL(15, 2) NOT NULL,
    UNIQUE(usuario_id, ano_mes),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);