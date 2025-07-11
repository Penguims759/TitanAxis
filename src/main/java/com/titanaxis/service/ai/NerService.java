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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private static final String MODEL_FILE = "ner-model.bin"; // NOVO: Ficheiro para guardar o modelo

    public NerService() {
        loadOrTrainModel(); // ALTERADO: Chama o novo método
    }

    // NOVO MÉTODO: Carrega o modelo se existir, senão, treina e guarda.
    private void loadOrTrainModel() {
        try {
            File modelFile = new File(MODEL_FILE);
            if (modelFile.exists()) {
                logger.info("A carregar modelo NER pré-treinado do ficheiro...");
                try (InputStream modelIn = new FileInputStream(modelFile)) {
                    nerModel = new TokenNameFinderModel(modelIn);
                    logger.info("Modelo NER carregado com sucesso.");
                }
            } else {
                logger.info("Nenhum modelo NER pré-treinado encontrado. A iniciar treino...");
                trainAndSaveModel(modelFile);
            }
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao carregar ou treinar o modelo NER.", e);
        }
    }

    // ALTERADO: Método agora guarda o modelo após o treino.
    private void trainAndSaveModel(File modelFile) throws java.io.IOException {
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

            // Salva o modelo treinado no ficheiro
            try (FileOutputStream modelOut = new FileOutputStream(modelFile)) {
                nerModel.serialize(modelOut);
                logger.info("Modelo NER guardado em: " + modelFile.getAbsolutePath());
            }
        }
    }

    public Map<String, String> extractEntities(String sentence) {
        if (nerModel == null) {
            logger.warning("O modelo NER não está disponível. A extração de entidades não pode ser realizada.");
            return Collections.emptyMap();
        }

        NameFinderME nameFinder = new NameFinderME(nerModel);
        String[] tokens = sentence.split("\\s+");
        Span[] nameSpans = nameFinder.find(tokens);

        Map<String, String> entities = new HashMap<>();
        for (Span span : nameSpans) {
            StringBuilder entityValue = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                entityValue.append(tokens[i]).append(" ");
            }
            String cleanedValue = entityValue.toString().trim()
                    .replaceFirst("^(cliente|lote|fornecedor)\\s", "");

            entities.put(span.getType(), cleanedValue);
        }
        return entities;
    }
}