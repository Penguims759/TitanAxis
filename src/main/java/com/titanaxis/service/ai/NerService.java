package com.titanaxis.service.ai;

import com.google.inject.Singleton;
import com.titanaxis.util.AppLogger;
import opennlp.tools.namefind.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class NerService {

    private static final Logger logger = AppLogger.getLogger();
    private TokenNameFinderModel nerModel;
    private static final String TRAINING_FILE = "/ai/ner-train.txt";

    public NerService() {
        trainModel();
    }

    private void trainModel() {
        try (InputStream dataIn = getClass().getResourceAsStream(TRAINING_FILE)) {
            if (dataIn == null) {
                throw new java.io.IOException("Ficheiro de treino NER não encontrado: " + TRAINING_FILE);
            }

            InputStreamFactory inputStreamFactory = () -> dataIn;
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            nerModel = NameFinderME.train("pt", null, sampleStream, params, TokenNameFinderFactory.create(null, null, Collections.emptyMap(), new BioCodec()));
            logger.info("Modelo de Reconhecimento de Entidades (NER) treinado com sucesso.");

        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao treinar o modelo NER.", e);
        }
    }

    public Map<String, String> extractEntities(String sentence) {
        if (nerModel == null) {
            logger.warning("O modelo NER não está disponível. A extração de entidades não pode ser realizada.");
            return Collections.emptyMap();
        }

        // *** INÍCIO DA CORREÇÃO ***
        NameFinderME nameFinder = new NameFinderME(nerModel); // Cria a ferramenta de busca a partir do modelo
        String[] tokens = sentence.split("\\s+");
        Span[] nameSpans = nameFinder.find(tokens); // Usa a ferramenta (nameFinder) e não o modelo (nerModel)
        // *** FIM DA CORREÇÃO ***

        Map<String, String> entities = new HashMap<>();
        for (Span span : nameSpans) {
            StringBuilder entityValue = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                entityValue.append(tokens[i]).append(" ");
            }
            // Limpa o valor da entidade de palavras comuns que podem ser apanhadas
            String cleanedValue = entityValue.toString().trim()
                    .replaceFirst("^(cliente|lote|fornecedor)\\s", "");

            entities.put(span.getType(), cleanedValue);
        }
        return entities;
    }
}