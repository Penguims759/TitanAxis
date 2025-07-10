package com.titanaxis.service.ai;

import com.google.inject.Singleton;
import com.titanaxis.service.Intent;
import com.titanaxis.util.AppLogger;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class NLPIntentService {

    private static final Logger logger = AppLogger.getLogger();
    private DocumentCategorizerME categorizer;
    private static final String TRAINING_FILE = "/ai/intent-train.txt";

    public NLPIntentService() {
        trainModel();
    }

    private void trainModel() {
        try (InputStream dataIn = getClass().getResourceAsStream(TRAINING_FILE)) {
            if (dataIn == null) {
                throw new IOException("Ficheiro de treino não encontrado: " + TRAINING_FILE);
            }

            InputStreamFactory inputStreamFactory = () -> dataIn;
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 1); // Reduzido para incluir todas as features com poucas amostras

            DoccatModel model = DocumentCategorizerME.train("pt", sampleStream, params, new DoccatFactory());

            this.categorizer = new DocumentCategorizerME(model);
            logger.info("Modelo de intenção treinado com sucesso.");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao treinar o modelo de intenção.", e);
        }
    }

    public Intent getIntent(String userInput) {
        if (categorizer == null) {
            logger.warning("O categorizador de intenção não está disponível. A retornar UNKNOWN.");
            return Intent.UNKNOWN;
        }
        try {
            double[] outcomes = categorizer.categorize(userInput.split("\\s+"));
            String bestCategory = categorizer.getBestCategory(outcomes);
            return Intent.valueOf(bestCategory);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Não foi possível determinar a intenção para a entrada: '" + userInput + "'.", e);
            return Intent.UNKNOWN;
        }
    }
}