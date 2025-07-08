// src/main/java/com/titanaxis/service/Intent.java
package com.titanaxis.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public enum Intent {
    // INTENÇÕES DE CRIAÇÃO
    CREATE_PRODUCT(List.of("criar produto", "novo produto", "cadastrar produto"), List.of()),
    CREATE_USER(List.of("criar utilizador", "novo utilizador", "adicionar utilizador"), List.of()),
    CREATE_CATEGORY(List.of("criar categoria", "nova categoria", "adicionar categoria"), List.of()),
    CREATE_CLIENT(List.of("criar cliente", "novo cliente", "adicionar cliente"), List.of()),
    CREATE_FORNECEDOR(List.of("criar fornecedor", "novo fornecedor", "cadastrar fornecedor"), List.of()), // ADICIONADO

    // INTENÇÃO DE VENDA
    START_SALE(List.of("inicie uma venda", "começar venda", "abrir venda", "nova venda para"), List.of("venda")),

    // INTENÇÕES DE ATUALIZAÇÃO E GESTÃO
    UPDATE_PRODUCT(List.of("alterar produto", "mudar produto", "atualizar produto", "alterar preco", "alterar status"), List.of()),
    MANAGE_STOCK(List.of("adicionar stock", "adicionar unidades", "adicionar ao lote", "gerir stock", "gerir estoque"), List.of("stock", "estoque", "lote")),
    ADJUST_STOCK(List.of("ajustar stock", "ajustar estoque", "corrigir stock", "corrigir estoque"), List.of("ajustar", "corrigir")),

    // INTENÇÕES DE CONSULTA
    QUERY_STOCK(List.of("qual o stock", "qual o estoque", "ver stock", "ver estoque"), List.of("stock", "estoque")),
    QUERY_CLIENT_DETAILS(List.of("detalhes do cliente", "ver cliente", "informacoes do cliente"), List.of("cliente")),
    QUERY_PRODUCT_LOTS(List.of("quais os lotes", "lotes do produto", "ver lotes"), List.of("lote")),
    QUERY_MOVEMENT_HISTORY(List.of("historico de movimentos", "ver historico"), List.of("historico")),
    QUERY_TOP_CLIENTS(List.of("quais os melhores clientes", "top clientes", "ranking de clientes"), List.of("cliente")),
    QUERY_TOP_PRODUCT(List.of("produto mais vendido", "qual o mais vendido"), List.of("vendido")),
    QUERY_CLIENT_HISTORY(List.of("historico de compras do cliente", "compras do cliente"), List.of("historico")),
    QUERY_LOW_STOCK(List.of("baixo estoque", "baixo stock", "pouco estoque", "pouco stock"), List.of()),
    QUERY_EXPIRING_LOTS(List.of("quais lotes estao para vencer", "lotes a vencer"), List.of("vencer", "validade")),

    // INTENÇÕES DE AÇÃO DIRETA E UI
    GENERATE_REPORT(List.of("gere relatorio", "gerar relatorio"), List.of("relatorio")),
    GUIDE_ADD_LOTE(List.of("como adicionar um lote", "ajuda para adicionar lote"), List.of("como", "ajuda", "lote")),
    GUIDE_ADD_PRODUCT(List.of("como adicionar um produto", "ajuda para criar produto"), List.of("como", "ajuda", "produto")),
    NAVIGATE_TO(List.of("ir para", "leve-me para", "abrir tela", "abrir ecra", "navegar para"), List.of("ir", "navegar")),
    CHANGE_THEME(List.of("mudar tema", "alterar tema", "tema claro", "tema escuro"), List.of("tema")),

    // INTENÇÕES GERAIS
    GREETING(List.of("ola", "oi", "bom dia", "boa tarde", "boa noite", "boas"), List.of()),
    CONFIRM(List.of("sim", "confirmo", "confirma", "pode", "ok", "isso", "exato"), List.of()),
    DENY(List.of("não", "nao", "cancela", "cancelar", "sair", "encerrar", "deixa"), List.of()),
    UNKNOWN(List.of(), List.of());

    private final List<String> phrases;
    private final List<String> coreTerms;

    Intent(List<String> phrases, List<String> coreTerms) {
        this.phrases = phrases;
        this.coreTerms = coreTerms;
    }

    // O resto da classe permanece igual
    public List<String> getKeywords() { return phrases; }
    public int getScore(String normalizedQuery) {
        if (this == UNKNOWN || this == GREETING || this == CONFIRM || this == DENY) return 0;
        int score = 0;
        for (String phrase : this.phrases) {
            if (normalizedQuery.contains(phrase)) {
                score += 20;
                if (Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b").matcher(normalizedQuery).find()) score += 10;
            }
        }
        for (String term : this.coreTerms) {
            if (normalizedQuery.contains(term)) score += 5;
        }
        List<String> negationWords = List.of("nao", "jamais", "nunca", "sem");
        for (String term : this.coreTerms) {
            if (normalizedQuery.contains(term)) {
                for (String neg : negationWords) if (normalizedQuery.contains(neg + " " + term)) score -= 40;
            }
        }
        return score;
    }
}