package com.titanaxis.service.ai;

import com.google.inject.Singleton;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
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
import org.slf4j.Logger;

@Singleton
public class NerService {

    private static final Logger logger = AppLogger.getLogger();
    private TokenNameFinderModel nerModel;

    public NerService() {
        loadOrTrainModel();
    }

    private void loadOrTrainModel() {
        // ALTERAÇÃO: Usa o locale completo (ex: "pt_BR") para o nome do ficheiro.
        String localeString = I18n.getCurrentLocale().toString(); // ex: "en_US"
        String modelFileName = "ner-model_" + localeString + ".bin";
        String trainingFileName = "/ai/ner-train_" + localeString + ".txt";

        try {
            File modelFile = new File(modelFileName);
            if (modelFile.exists()) {
                logger.info("A carregar modelo NER pré-treinado do ficheiro: " + modelFileName);
                try (InputStream modelIn = new FileInputStream(modelFile)) {
                    nerModel = new TokenNameFinderModel(modelIn);
                    logger.info("Modelo NER carregado com sucesso.");
                }
            } else {
                logger.info("Nenhum modelo NER pré-treinado encontrado para o locale '" + localeString + "'. A iniciar treino...");
                trainAndSaveModel(modelFile, trainingFileName, I18n.getCurrentLocale().getLanguage());
            }
        } catch (java.io.IOException e) {
            logger.error("Falha crítica ao carregar ou treinar o modelo NER para o locale " + localeString, e);
        }
    }

    private void trainAndSaveModel(File modelFile, String trainingFile, String lang) throws java.io.IOException {
        try (InputStream dataIn = getClass().getResourceAsStream(trainingFile)) {
            if (dataIn == null) {
                logger.warn("Ficheiro de treino NER não encontrado: " + trainingFile + ". A usar o ficheiro de treino padrão (pt_BR).");
                trainAndSaveModel(new File("ner-model_pt_BR.bin"), "/ai/ner-train_pt_BR.txt", "pt");
                return;
            }

            InputStreamFactory inputStreamFactory = () -> dataIn;
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            nerModel = NameFinderME.train(lang, null, sampleStream, params, TokenNameFinderFactory.create(null, null, Collections.emptyMap(), new BioCodec()));
            logger.info("Modelo de Reconhecimento de Entidades (NER) para o idioma '" + lang + "' treinado com sucesso.");

            try (FileOutputStream modelOut = new FileOutputStream(modelFile)) {
                nerModel.serialize(modelOut);
                logger.info("Modelo NER guardado em: " + modelFile.getAbsolutePath());
            }
        }
    }

    public Map<String, String> extractEntities(String sentence) {
        if (nerModel == null) {
            logger.warn("O modelo NER não está disponível. A extração de entidades não pode ser realizada.");
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
                    .replaceFirst("^(cliente|lote|fornecedor|client|batch|supplier)\\s", "");

            entities.put(span.getType(), cleanedValue);
        }
        return entities;
    }
}