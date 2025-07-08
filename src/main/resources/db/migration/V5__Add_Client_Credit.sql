-- Adiciona a coluna de crédito na tabela de clientes para gestão de devoluções
ALTER TABLE clientes ADD COLUMN credito DECIMAL(10, 2) NOT NULL DEFAULT 0.00;