-- Tabela de usuários: Armazena as credenciais e permissões dos utilizadores.
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome_usuario TEXT NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL,
    nivel_acesso TEXT NOT NULL DEFAULT 'padrao'
);

-- Tabela de categorias: Agrupa os produtos.
CREATE TABLE IF NOT EXISTS categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE
);

-- Tabela de produtos: Contém os detalhes de cada produto.
CREATE TABLE IF NOT EXISTS produtos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT,
    preco REAL NOT NULL,
    categoria_id INTEGER,
    ativo INTEGER NOT NULL DEFAULT 1, -- 1 para ativo, 0 para inativo
    data_adicao TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_atualizacao TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- Tabela de lotes de estoque: Controla o stock por lotes, com quantidade e validade.
CREATE TABLE IF NOT EXISTS estoque_lotes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    produto_id INTEGER NOT NULL,
    numero_lote TEXT NOT NULL,
    quantidade INTEGER NOT NULL,
    data_validade TEXT,
    data_entrada TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    UNIQUE(produto_id, numero_lote)
);

-- Gatilho (Trigger) para atualizar automaticamente a data de modificação de um produto.
CREATE TRIGGER IF NOT EXISTS trg_produtos_update
AFTER UPDATE ON produtos
FOR EACH ROW
BEGIN
    UPDATE produtos SET data_ultima_atualizacao = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- Tabela de movimentos de estoque: Regista todas as entradas e saídas de stock.
CREATE TABLE IF NOT EXISTS movimentos_estoque (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    produto_id INTEGER NOT NULL,
    lote_id INTEGER,
    tipo_movimento TEXT NOT NULL, -- Ex: 'VENDA', 'ENTRADA DE LOTE'
    quantidade INTEGER NOT NULL,
    data_movimento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de clientes: Armazena os dados dos clientes.
CREATE TABLE IF NOT EXISTS clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    contato TEXT,
    endereco TEXT
);

-- Tabela de vendas: Regista o cabeçalho de cada transação de venda.
CREATE TABLE IF NOT EXISTS vendas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER,
    usuario_id INTEGER,
    data_venda TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total REAL NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de itens da venda: Detalha os produtos e lotes de cada venda.
CREATE TABLE IF NOT EXISTS venda_itens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venda_id INTEGER NOT NULL,
    produto_id INTEGER NOT NULL,
    lote_id INTEGER,
    quantidade INTEGER NOT NULL,
    preco_unitario REAL NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE RESTRICT,
    FOREIGN KEY (lote_id) REFERENCES estoque_lotes(id) ON DELETE RESTRICT
);

-- Tabela de auditoria: Regista todas as ações importantes realizadas no sistema.
CREATE TABLE IF NOT EXISTS auditoria_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_evento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER,
    usuario_nome TEXT,
    acao TEXT NOT NULL,
    entidade TEXT NOT NULL,
    detalhes TEXT
);