-- Adiciona a coluna para associar um movimento de estoque a uma venda específica
ALTER TABLE movimentos_estoque ADD COLUMN venda_id INT NULL;

-- Adiciona a chave estrangeira para garantir a integridade dos dados
ALTER TABLE movimentos_estoque ADD CONSTRAINT fk_movimento_venda
FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE SET NULL;