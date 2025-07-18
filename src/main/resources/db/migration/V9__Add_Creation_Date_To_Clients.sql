-- V10: Adiciona a data de criação à tabela de clientes para uma contagem de "novos clientes" mais precisa.
ALTER TABLE clientes ADD COLUMN data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Define uma data de criação para os registos existentes para que possam ser contabilizados.
UPDATE clientes SET data_criacao = '2025-07-18 10:00:00' WHERE id IN (1, 2, 3, 4);