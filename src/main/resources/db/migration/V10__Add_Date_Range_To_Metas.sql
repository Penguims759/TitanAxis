-- V11: Altera a tabela de metas para suportar períodos de datas personalizadas (Versão Definitiva)

-- Passo 1: Remove a FOREIGN KEY constraint que depende do índice que precisamos de remover.
-- O nome 'metas_venda_ibfk_1' é o nome padrão que o MariaDB cria.
ALTER TABLE metas_venda DROP FOREIGN KEY metas_venda_ibfk_1;

-- Passo 2: Remove o índice UNIQUE antigo, que foi criado em V4. O nome padrão é o da primeira coluna.
ALTER TABLE metas_venda DROP INDEX usuario_id;

-- Passo 3: Agora, removemos a coluna 'ano_mes' com segurança.
ALTER TABLE metas_venda DROP COLUMN ano_mes;

-- Passo 4: Adiciona as novas colunas para o período.
ALTER TABLE metas_venda ADD COLUMN data_inicio DATE NOT NULL AFTER usuario_id;
ALTER TABLE metas_venda ADD COLUMN data_fim DATE NOT NULL AFTER data_inicio;

-- Passo 5: Recria a FOREIGN KEY para a coluna 'usuario_id', agora com um nome explícito.
ALTER TABLE metas_venda ADD CONSTRAINT fk_metas_usuario
FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

-- Passo 6: Adiciona a nova restrição de unicidade para garantir que não há metas duplicadas para o mesmo utilizador e período.
ALTER TABLE metas_venda ADD CONSTRAINT uq_usuario_periodo UNIQUE (usuario_id, data_inicio, data_fim);