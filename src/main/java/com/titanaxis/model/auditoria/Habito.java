package com.titanaxis.model.auditoria;

import com.titanaxis.model.Usuario;
import com.titanaxis.util.I18n;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Table(name = "habitos_usuario")
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String acao;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_da_semana", nullable = false)
    private DayOfWeek diaDaSemana;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoHabito tipo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    public enum TipoHabito {
        AUTOMATICO, MANUAL
    }

    public Habito() {
        this.dataCriacao = LocalDateTime.now();
    }

    public String getSugestao() {
        if ("GENERATE_REPORT".equalsIgnoreCase(acao)) {
            return I18n.getString("habit.suggestion.generateReport");
        }
        return null;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public DayOfWeek getDiaDaSemana() { return diaDaSemana; }
    public void setDiaDaSemana(DayOfWeek diaDaSemana) { this.diaDaSemana = diaDaSemana; }
    public TipoHabito getTipo() { return tipo; }
    public void setTipo(TipoHabito tipo) { this.tipo = tipo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}