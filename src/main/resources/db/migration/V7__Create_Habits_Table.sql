-- V7: Cria a tabela para armazenar hábitos de utilizador, aprendidos ou manuais.
CREATE TABLE habitos_usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    acao VARCHAR(255) NOT NULL,
    dia_da_semana VARCHAR(20) NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- 'AUTOMATICO' ou 'MANUAL'
    data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE(usuario_id, acao, dia_da_semana)
);