// src/main/java/com/titanaxis/util/PasswordUtil.java
package com.titanaxis.util; // ALTERADO

import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe utilitária para hashing e verificação de senhas usando BCrypt.
 * BCrypt é um algoritmo de hashing de senha seguro e resistente a ataques de força bruta.
 */
public class PasswordUtil {

    /**
     * Gera um hash seguro para a senha em texto simples.
     * O salt é gerado automaticamente pelo BCrypt.
     * @param plainPassword A senha em texto simples.
     * @return O hash da senha.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifica se uma senha em texto simples corresponde a um hash dado.
     * @param plainPassword A senha em texto simples a ser verificada.
     * @param hashedPassword O hash da senha armazenado (inclui o salt).
     * @return true se a senha corresponder ao hash, false caso contrário.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}