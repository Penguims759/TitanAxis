// src/main/java/com/titanaxis/util/StringUtil.java
package com.titanaxis.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class StringUtil {

    private StringUtil() {}

    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Extrai a palavra que vem a seguir a uma palavra-chave, com tolerância a erros.
     * Ex: extractFuzzyValueAfter("... com sneha 123...", "senha") -> "123"
     * @param text O texto completo a ser analisado.
     * @param targetKeyword A palavra-chave a ser procurada.
     * @return A palavra seguinte, ou null se não for encontrada.
     */
    public static String extractFuzzyValueAfter(String text, String targetKeyword) {
        String[] words = normalize(text).split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (levenshteinDistance(words[i], targetKeyword) <= 1) {
                if (i + 1 < words.length) {
                    return words[i + 1];
                }
            }
        }
        return null;
    }

    // ... (restante dos métodos isNumeric, normalize, levenshteinDistance sem alteração) ...
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
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
        return withoutAccents.toLowerCase();
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