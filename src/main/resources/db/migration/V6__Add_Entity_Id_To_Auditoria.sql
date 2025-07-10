-- V6: Adiciona a coluna entidade_id para permitir uma análise de hábitos mais detalhada.
ALTER TABLE auditoria_logs ADD COLUMN entidade_id INT NULL AFTER entidade;

-- No futuro, poderíamos adicionar um índice para otimizar as consultas.
-- CREATE INDEX idx_auditoria_habits ON auditoria_logs (usuario_id, acao, entidade, entidade_id, data_evento);