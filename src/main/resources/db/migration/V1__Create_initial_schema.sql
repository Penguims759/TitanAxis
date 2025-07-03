-- Tabela de usuários
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_usuario VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    nivel_acesso VARCHAR(50) NOT NULL DEFAULT 'padrao'
);

-- Tabela de categorias
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela de produtos
CREATE TABLE produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    categoria_id INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_adicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- Tabela de lotes de estoque
CREATE TABLE estoque_lotes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT NOT NULL,
    numero_lote VARCHAR(255) NOT NULL,
    quantidade INT NOT NULL,
    data_validade DATE,
    data_entrada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    UNIQUE(produto_id, numero_lote)
);

-- Gatilho (Trigger) para produtos no formato MariaDB
CREATE TRIGGER trg_produtos_update
BEFORE UPDATE ON produtos
FOR EACH ROW
SET NEW.data_ultima_atualizacao = CURRENT_TIMESTAMP;

-- Tabela de movimentos de estoque
CREATE TABLE movimentos_estoque (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT NOT NULL,
    lote_id INT,
    tipo_movimento VARCHAR(50) NOT NULL,
    quantidade INT NOT NULL,
    data_movimento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de clientes
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    contato VARCHAR(255),
    endereco TEXT
);

-- Tabela de vendas
CREATE TABLE vendas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    usuario_id INT,
    data_venda DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de itens da venda
CREATE TABLE venda_itens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    produto_id INT NOT NULL,
    lote_id INT,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE RESTRICT,
    FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE RESTRICT
);

-- Tabela de auditoria
CREATE TABLE auditoria_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT,
    usuario_nome VARCHAR(255),
    acao VARCHAR(255) NOT NULL,
    entidade VARCHAR(255) NOT NULL,
    detalhes TEXT
);