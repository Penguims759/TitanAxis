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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final String MODEL_FILE = "nlp-intent-model.bin"; // NOVO: Ficheiro para guardar o modelo

    public NLPIntentService() {
        loadOrTrainModel(); // ALTERADO: Chama o novo método
    }

    // NOVO MÉTODO: Carrega o modelo se existir, senão, treina e guarda.
    private void loadOrTrainModel() {
        try {
            File modelFile = new File(MODEL_FILE);
            if (modelFile.exists()) {
                logger.info("A carregar modelo de intenção pré-treinado do ficheiro...");
                try (InputStream modelIn = new FileInputStream(modelFile)) {
                    DoccatModel model = new DoccatModel(modelIn);
                    this.categorizer = new DocumentCategorizerME(model);
                    logger.info("Modelo de intenção carregado com sucesso.");
                }
            } else {
                logger.info("Nenhum modelo pré-treinado encontrado. A iniciar treino...");
                trainAndSaveModel(modelFile);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao carregar ou treinar o modelo de intenção.", e);
        }
    }

    // ALTERADO: Método agora guarda o modelo após o treino.
    private void trainAndSaveModel(File modelFile) throws IOException {
        try (InputStream dataIn = getClass().getResourceAsStream(TRAINING_FILE)) {
            if (dataIn == null) {
                throw new IOException("Ficheiro de treino não encontrado: " + TRAINING_FILE);
            }

            InputStreamFactory inputStreamFactory = () -> dataIn;
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            DoccatModel model = DocumentCategorizerME.train("pt", sampleStream, params, new DoccatFactory());
            this.categorizer = new DocumentCategorizerME(model);
            logger.info("Modelo de intenção treinado com sucesso.");

            // Salva o modelo treinado no ficheiro
            try (FileOutputStream modelOut = new FileOutputStream(modelFile)) {
                model.serialize(modelOut);
                logger.info("Modelo de intenção guardado em: " + modelFile.getAbsolutePath());
            }
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