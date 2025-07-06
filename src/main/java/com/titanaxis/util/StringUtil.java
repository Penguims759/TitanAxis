// src/main/java/com/titanaxis/util/StringUtil.java
package com.titanaxis.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class StringUtil {

    private StringUtil() {} // Impede a instanciação

    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Normaliza uma string, removendo acentos, cedilha e convertendo para minúsculas.
     * Exemplo: "Relatório de Vendas" -> "relatorio de vendas"
     * @param input A string a ser normalizada.
     * @return A string normalizada.
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICAL_MARKS_PATTERN.matcher(nfdNormalizedString).replaceAll("");
        return withoutAccents.toLowerCase();
    }

    /**
     * Calcula a Distância de Levenshtein entre duas strings.
     * A distância representa o número de edições (inserções, deleções, substituições)
     * necessárias para transformar uma string na outra.
     * @param s1 A primeira string.
     * @param s2 A segunda string.
     * @return A distância de Levenshtein.
     */
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