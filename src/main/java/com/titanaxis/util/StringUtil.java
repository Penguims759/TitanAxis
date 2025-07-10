// src/main/java/com/titanaxis/util/StringUtil.java
package com.titanaxis.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtil {

    private StringUtil() {}

    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Extrai o texto que vem a seguir a uma de várias palavras-chave, limpando o resultado final.
     * Ex: extractValueAfter("... ver lotes de caneta azul?", new String[]{"de", "do", "da"}) -> "caneta azul"
     * @param originalText O texto completo a ser analisado.
     * @param keywords As palavras-chave a serem procuradas.
     * @return O resto do texto limpo, ou null se não for encontrada.
     */
    public static String extractValueAfter(String originalText, String[] keywords) {
        if (originalText == null || keywords == null || keywords.length == 0) {
            return null;
        }

        String lowerCaseText = originalText.toLowerCase();
        int bestMatchIndex = -1;

        for (String keyword : keywords) {
            String spacedKeyword = " " + keyword + " ";
            int index = lowerCaseText.indexOf(spacedKeyword);
            if (index != -1) {
                bestMatchIndex = index + spacedKeyword.length();
                break; // Encontrou a primeira correspondência
            }
        }

        if (bestMatchIndex != -1) {
            String extracted = originalText.substring(bestMatchIndex).trim();
            // Remove pontuação comum do final da string
            return extracted.replaceAll("[?.,!]$", "").trim();
        }
        return null;
    }


    /**
     * @deprecated Use extractValueAfter para uma correspondência mais simples ou um extrator de entidade de PNL para mais complexidade.
     */
    @Deprecated
    public static String extractFuzzyValueAfter(String text, String targetKeyword) {
        String normalizedText = normalize(text);
        String[] words = normalizedText.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (levenshteinDistance(words[i], targetKeyword) <= 1) {
                if (i + 1 < words.length) {
                    return Arrays.stream(words, i + 1, words.length)
                            .collect(Collectors.joining(" "));
                }
            }
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) { // Adicionado trim() e verificação de vazio
            return false;
        }
        try {
            Double.parseDouble(str.replace(",", "."));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICAL_MARKS_PATTERN.matcher(nfdNormalizedString).replaceAll("");
        return withoutAccents.toLowerCase().trim();
    }

    public static int levenshteinDistance(String s1, String s2) {
        s1 = normalize(s1);
        s2 = normalize(s2);

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}