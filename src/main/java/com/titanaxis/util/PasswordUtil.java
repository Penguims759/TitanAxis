package com.titanaxis.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe utilitária para hashing e verificação de senhas usando BCrypt.
 * BCrypt é um algoritmo de hashing de senha seguro que inclui automaticamente
 * um "sal" aleatório em cada hash gerado.
 */
public class PasswordUtil {

    /**
     * Gera um hash seguro para a senha em texto simples.
     * O "sal" é gerado e incluído no hash resultante pela própria biblioteca.
     * @param plainPassword A senha em texto simples.
     * @return O hash da senha, contendo o "sal".
     */
    public static String hashPassword(String plainPassword) {
        // O método gensalt() gera um sal aleatório.
        // O método hashpw() combina a senha e o sal para criar o hash final.
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)); // A força 12 é um bom padrão.
    }

    /**
     * Verifica se uma senha em texto simples corresponde a um hash dado.
     * A biblioteca extrai automaticamente o "sal" do hash armazenado para fazer a comparação.
     * @param plainPassword A senha em texto simples a ser verificada.
     * @param hashedPassword O hash da senha armazenado.
     * @return true se a senha corresponder ao hash, false caso contrário.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Isto pode acontecer se o hash armazenado estiver mal formatado.
            // É uma boa prática apanhar a exceção para evitar que a aplicação quebre.
            return false;
        }
    }
}