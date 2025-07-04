// File: penguims759/titanaxis/Penguims759-TitanAxis-5e774d0e21ca474f2c1a48a6f8706ffbdf671398/src/main/java/com/titanaxis/util/UIMessageUtil.java
package com.titanaxis.util;

import javax.swing.*;
import java.awt.*;

/**
 * Classe utilitária para exibir mensagens e diálogos de confirmação na UI.
 * Centraliza a lógica de feedback ao utilizador para garantir consistência.
 */
public class UIMessageUtil {

    /**
     * Exibe uma mensagem informativa.
     * @param parentComponent O componente pai para o diálogo (pode ser null).
     * @param message A mensagem a ser exibida.
     * @param title O título do diálogo.
     */
    public static void showInfoMessage(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe uma mensagem de erro.
     * @param parentComponent O componente pai para o diálogo (pode ser null).
     * @param message A mensagem de erro a ser exibida.
     * @param title O título do diálogo de erro.
     */
    public static void showErrorMessage(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Exibe uma mensagem de aviso.
     * @param parentComponent O componente pai para o diálogo (pode ser null).
     * @param message A mensagem de aviso a ser exibida.
     * @param title O título do diálogo de aviso.
     */
    public static void showWarningMessage(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Exibe uma mensagem genérica (plain message).
     * @param parentComponent O componente pai para o diálogo (pode ser null).
     * @param message A mensagem a ser exibida.
     * @param title O título do diálogo.
     */
    public static void showPlainMessage(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Exibe um diálogo de confirmação.
     * @param parentComponent O componente pai para o diálogo (pode ser null).
     * @param message A mensagem de confirmação.
     * @param title O título do diálogo.
     * @return true se o utilizador clicou em "Sim", false caso contrário.
     */
    public static boolean showConfirmDialog(Component parentComponent, String message, String title) {
        return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}