package com.titanaxis.service;

import com.titanaxis.util.StringUtil;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representa a intenção do utilizador.
 * A lógica foi refatorada para usar padrões de gatilho (trigger) e termos de pontuação,
 * tornando a identificação da intenção muito mais precisa e robusta.
 */
public enum Intent {

    // --- INTENÇÕES DE CRIAÇÃO ---
    CREATE_PRODUCT(List.of("criar produto", "novo produto", "cadastrar produto")),
    CREATE_USER(List.of("criar utilizador", "novo utilizador", "adicionar utilizador")),
    CREATE_CATEGORY(List.of("criar categoria", "nova categoria", "adicionar categoria")),
    CREATE_CLIENT(List.of("criar cliente", "novo cliente", "adicionar cliente")),
    CREATE_FORNECEDOR(List.of("criar fornecedor", "novo fornecedor", "cadastrar fornecedor")),

    // --- INTENÇÃO DE VENDA ---
    START_SALE(List.of("inicie venda", "começar venda", "abrir venda", "nova venda"),
            List.of("para cliente", "para ele"),
            Pattern.compile("(?:para|para o cliente)\\s+(.+)")),

    // --- INTENÇÕES DE ATUALIZAÇÃO E GESTÃO ---
    UPDATE_PRODUCT(List.of("alterar produto", "mudar produto", "atualizar produto"), List.of("preco", "status")),
    MANAGE_STOCK(List.of("adicionar stock", "adicionar estoque", "entrada de stock", "entrada de estoque"), List.of("unidades", "lote")),
    ADJUST_STOCK(List.of("ajustar stock", "ajustar estoque", "corrigir stock", "corrigir estoque"), List.of("total", "quantidade")),

    // --- INTENÇÕES DE CONSULTA (QUERY) ---
    QUERY_STOCK(List.of("qual o stock", "qual o estoque", "ver stock", "ver estoque", "stock de"),
            List.of("produto", "item"),
            Pattern.compile("(?:do produto|de|da)\\s+(.+)")),

    QUERY_CLIENT_DETAILS(List.of("detalhes do cliente", "ver cliente", "informacoes do cliente", "detalhes de"),
            List.of(),
            Pattern.compile("(?:do cliente|de|da)\\s+(.+)")),

    QUERY_PRODUCT_LOTS(List.of("quais os lotes", "lotes do produto", "ver lotes", "lotes de", "lotes dele"),
            List.of(),
            Pattern.compile("(?:do produto|de|da)\\s+(.+)")),

    QUERY_MOVEMENT_HISTORY(List.of("historico de movimentos", "ver historico", "historico dele"),
            List.of("produto"),
            Pattern.compile("(?:do produto|de|da)\\s+(.+)")),

    QUERY_TOP_CLIENTS(List.of("quais os melhores clientes", "top clientes", "ranking de clientes")),
    QUERY_TOP_PRODUCT(List.of("produto mais vendido", "qual o mais vendido")),
    QUERY_CLIENT_HISTORY(List.of("historico de compras", "compras do cliente", "historico dele"),
            List.of(),
            Pattern.compile("(?:do cliente|de|da)\\s+(.+)")),

    QUERY_LOW_STOCK(List.of("baixo estoque", "baixo stock", "pouco estoque", "pouco stock")),
    QUERY_EXPIRING_LOTS(List.of("quais lotes estao para vencer", "lotes a vencer", "lotes proximos da validade")),

    // --- INTENÇÕES DE AÇÃO DIRETA E UI ---
    GENERATE_REPORT(List.of("gere relatorio", "gerar relatorio"), List.of("vendas", "inventario", "pdf", "csv")),
    GUIDE_ADD_LOTE(List.of("como adicionar um lote", "ajuda para adicionar lote")),
    GUIDE_ADD_PRODUCT(List.of("como adicionar um produto", "ajuda para criar produto")),
    NAVIGATE_TO(List.of("ir para", "leve-me para", "abrir tela", "abrir ecra", "navegar para")),
    CHANGE_THEME(List.of("mudar tema", "alterar tema", "tema claro", "tema escuro")),

    // --- INTENÇÕES GERAIS ---
    GREETING(List.of("ola", "oi", "bom dia", "boa tarde", "boa noite", "boas")),
    CONFIRM(List.of("sim", "confirmo", "confirma", "pode", "ok", "isso", "exato")),
    DENY(List.of("não", "nao", "cancela", "cancelar", "sair", "encerrar", "deixa")),
    UNKNOWN(List.of());

    private final List<String> triggerPatterns;
    private final List<String> scoringTerms;
    private final Pattern entityExtractionPattern;

    Intent(List<String> triggerPatterns, List<String> scoringTerms, Pattern entityExtractionPattern) {
        this.triggerPatterns = triggerPatterns;
        this.scoringTerms = scoringTerms;
        this.entityExtractionPattern = entityExtractionPattern;
    }

    Intent(List<String> triggerPatterns, List<String> scoringTerms) {
        this(triggerPatterns, scoringTerms, null);
    }

    Intent(List<String> triggerPatterns) {
        this(triggerPatterns, List.of(), null);
    }

    public List<String> getKeywords() {
        return triggerPatterns;
    }

    public Optional<String> extractEntity(String normalizedQuery) {
        if (this.entityExtractionPattern != null) {
            Matcher matcher = this.entityExtractionPattern.matcher(normalizedQuery);
            if (matcher.find() && matcher.groupCount() > 0) {
                return Optional.of(matcher.group(1).trim());
            }
        }
        return Optional.empty();
    }

    public int getScore(String normalizedQuery) {
        // A intenção só é uma candidata se um dos seus padrões de gatilho for encontrado.
        boolean triggerFound = this.triggerPatterns.stream()
                .anyMatch(normalizedQuery::contains);

        if (!triggerFound) {
            return 0;
        }

        // Se um gatilho foi encontrado, a pontuação base é alta.
        int score = 50;

        // Termos de pontuação adicionam pontos para desambiguação.
        for (String term : this.scoringTerms) {
            if (normalizedQuery.contains(term)) {
                score += 10;
            }
        }

        // Palavras de negação diminuem drasticamente a pontuação para evitar falsos positivos.
        List<String> negationWords = List.of("nao", "jamais", "nunca", "sem");
        if (negationWords.stream().anyMatch(normalizedQuery::contains)) {
            score -= 40;
        }

        return score;
    }
}