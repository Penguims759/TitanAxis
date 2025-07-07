package com.titanaxis.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public enum Intent {
    UPDATE_PRODUCT(List.of("alterar produto", "mudar produto", "alterar preco", "alterar status", "inativar produto", "ativar produto"), List.of("produto")),
    MANAGE_STOCK(List.of("adicionar stock", "adicionar unidades", "adicionar ao lote", "gerir stock", "gerir estoque"), List.of("stock", "estoque", "unidade", "lote")),
    CREATE_PRODUCT(List.of("criar produto", "novo produto", "cadastrar produto"), List.of("produto")),
    CREATE_USER(List.of("criar utilizador", "novo utilizador", "adicionar utilizador", "criar usuario", "novo usuario", "adicionar usuario"), List.of("utilizador", "usuario")),
    CREATE_CATEGORY(List.of("criar categoria", "nova categoria", "adicionar categoria"), List.of("categoria")),
    CREATE_CLIENT(List.of("criar cliente", "novo cliente", "adicionar cliente"), List.of("cliente")),
    QUERY_STOCK(List.of("qual o stock", "qual o estoque", "stock de", "estoque de", "ver stock", "ver estoque"), List.of("stock", "estoque")),
    QUERY_CLIENT_DETAILS(List.of("detalhes do cliente", "mostre o cliente", "ver cliente", "informacoes do cliente"), List.of("cliente")),
    QUERY_PRODUCT_LOTS(List.of("quais os lotes", "lotes do produto", "ver lotes", "mostrar lotes"), List.of("lote")),
    QUERY_MOVEMENT_HISTORY(List.of("historico de movimentos", "ver historico", "mostre o historico"), List.of("historico", "movimento")),
    QUERY_TOP_CLIENTS(List.of("melhores clientes", "top clientes", "ranking de clientes", "cliente que mais compra"), List.of("cliente")),
    NAVIGATE_TO(List.of("ir para", "leve-me para", "abrir tela", "abrir ecra", "navegar para", "ver vendas", "ver clientes", "ver produtos", "ver relatorios"), List.of("ir", "navegar", "abrir", "ver")),
    GUIDE_ADD_LOTE(List.of("como adicionar lote", "ajuda lote", "como faco para adicionar lote"), List.of("como", "ajuda")),
    CHANGE_THEME(List.of("mudar tema", "alterar tema", "tema claro", "tema escuro"), List.of("tema")),
    GREETING(List.of("ola", "oi", "bom dia", "boa tarde", "boa noite", "boas"), List.of()),
    CONFIRM(List.of("sim", "confirmo", "confirma", "pode", "ok", "isso", "exato"), List.of()),
    DENY(List.of("não", "nao", "cancela", "cancelar", "sair", "para", "encerrar", "deixa"), List.of()),
    UNKNOWN(List.of(), List.of());

    private final List<String> phrases;
    private final List<String> coreTerms;

    Intent(List<String> phrases, List<String> coreTerms) {
        this.phrases = phrases;
        this.coreTerms = coreTerms;
    }

    public List<String> getKeywords() {
        return phrases;
    }

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