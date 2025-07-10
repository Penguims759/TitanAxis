package com.titanaxis.model.auditoria;

import java.time.DayOfWeek;
import java.util.Objects;

public class HabitoUsuario {
    private final String acao;
    private final DayOfWeek diaDaSemana;

    public HabitoUsuario(String acao, DayOfWeek diaDaSemana) {
        this.acao = acao;
        this.diaDaSemana = diaDaSemana;
    }

    public String getAcao() {
        return acao;
    }

    public DayOfWeek getDiaDaSemana() {
        return diaDaSemana;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitoUsuario that = (HabitoUsuario) o;
        return Objects.equals(acao, that.acao) && diaDaSemana == that.diaDaSemana;
    }

    @Override
    public int hashCode() {
        return Objects.hash(acao, diaDaSemana);
    }

    public String getSugestao() {
        if ("GENERATE_REPORT".equalsIgnoreCase(acao)) {
            return "* **Sugestão de Hábito:** Notei que costuma gerar relatórios neste dia. Deseja que eu prepare algum para si?";
        }
        // Adicionar mais sugestões personalizadas para outras ações no futuro.
        return null;
    }
}