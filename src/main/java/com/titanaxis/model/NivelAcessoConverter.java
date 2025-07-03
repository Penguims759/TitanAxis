package com.titanaxis.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

/**
 * Este conversor ensina o JPA/Hibernate a mapear o nosso enum NivelAcesso
 * para a sua representação em string (minúsculas) na base de dados, e vice-versa,
 * tratando a conversão de forma insensível a maiúsculas/minúsculas.
 */
@Converter(autoApply = true) // autoApply=true faz com que seja usado em todos os campos NivelAcesso
public class NivelAcessoConverter implements AttributeConverter<NivelAcesso, String> {

    /**
     * Converte o valor do enum para a string que será guardada na base de dados.
     * Ex: NivelAcesso.ADMIN -> "admin"
     */
    @Override
    public String convertToDatabaseColumn(NivelAcesso nivelAcesso) {
        if (nivelAcesso == null) {
            return null;
        }
        return nivelAcesso.getNome();
    }

    /**
     * Converte a string da base de dados de volta para o enum correspondente.
     * Ex: "admin" -> NivelAcesso.ADMIN
     */
    @Override
    public NivelAcesso convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Itera sobre todos os valores do enum e encontra aquele cujo 'nome' corresponde
        // à string da base de dados, ignorando as diferenças de maiúsculas/minúsculas.
        return Stream.of(NivelAcesso.values())
                .filter(c -> c.getNome().equalsIgnoreCase(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}