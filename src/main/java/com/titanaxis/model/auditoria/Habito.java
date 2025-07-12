// penguims759/titanaxis/Penguims759-TitanAxis-3281ebcc37f2e4fc4ae9f1a9f16e291130f76009/src/main/java/com/titanaxis/model/auditoria/Habito.java
package com.titanaxis.model.auditoria;

import com.titanaxis.util.I18n; // Importado
import jakarta.persistence.*;

import java.time.DayOfWeek;

@Entity
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int usuarioId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek diaDaSemana;

    private String acaoFrequente; // Ex: "GERAR_RELATORIO_VENDAS"

    private int contagem;

    public String getSugestao() {
        if ("GERAR_RELATORIO_VENDAS".equals(acaoFrequente)) {
            return I18n.getString("habit.suggestion.generateReport"); // ALTERADO
        }
        return null; // Ou outras sugestões baseadas na ação
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public DayOfWeek getDiaDaSemana() { return diaDaSemana; }
    public void setDiaDaSemana(DayOfWeek diaDaSemana) { this.diaDaSemana = diaDaSemana; }
    public String getAcaoFrequente() { return acaoFrequente; }
    public void setAcaoFrequente(String acaoFrequente) { this.acaoFrequente = acaoFrequente; }
    public int getContagem() { return contagem; }
    public void setContagem(int contagem) { this.contagem = contagem; }
}