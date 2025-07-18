-- V8: Adiciona informações de pagamento e parcelamento na tabela de vendas
ALTER TABLE vendas ADD COLUMN forma_pagamento VARCHAR(50) NOT NULL DEFAULT 'A_VISTA';
ALTER TABLE vendas ADD COLUMN numero_parcelas INT NOT NULL DEFAULT 1;