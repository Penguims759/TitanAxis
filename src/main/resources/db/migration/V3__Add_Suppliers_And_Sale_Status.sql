-- src/main/resources/db/migration/V3__Add_Suppliers_And_Sale_Status.sql

-- Tabela de Fornecedores
CREATE TABLE fornecedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    cnpj VARCHAR(20),
    contato_nome VARCHAR(255),
    contato_telefone VARCHAR(50),
    contato_email VARCHAR(255),
    endereco TEXT
);

-- Adiciona a coluna de fornecedor na tabela de produtos
ALTER TABLE produtos ADD COLUMN fornecedor_id INT NULL;

-- Adiciona a chave estrangeira para garantir a integridade dos dados
ALTER TABLE produtos ADD CONSTRAINT fk_produto_fornecedor
FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id) ON DELETE SET NULL;

-- Adiciona a coluna de status na tabela de vendas
ALTER TABLE vendas ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'FINALIZADA';