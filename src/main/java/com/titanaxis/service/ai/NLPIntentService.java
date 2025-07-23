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
    // ALTERADO: O modelo agora é procurado dentro do classpath.
    private static final String MODEL_RESOURCE_PATH = "/ai/nlp-intent-model.bin";

    public NLPIntentService() {
        loadOrTrainModel();
    }

    private void loadOrTrainModel() {
        try (InputStream modelIn = getClass().getResourceAsStream(MODEL_RESOURCE_PATH)) {
            if (modelIn != null) {
                logger.info("A carregar modelo de intenção pré-treinado do classpath...");
                DoccatModel model = new DoccatModel(modelIn);
                this.categorizer = new DocumentCategorizerME(model);
                logger.info("Modelo de intenção carregado com sucesso.");
            } else {
                logger.warning("Modelo de intenção não encontrado no classpath: " + MODEL_RESOURCE_PATH + ". A tentar treinar um novo modelo...");
                // Se não encontrar o modelo, treina um novo a partir do ficheiro de treino
                trainAndLog();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao carregar o modelo de intenção. A tentar treinar um novo.", e);
            trainAndLog();
        }
    }

    private void trainAndLog() {
        try {
            trainModel();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Falha crítica ao treinar o modelo de intenção. O serviço de NLP pode não funcionar.", e);
        }
    }

    private void trainModel() throws IOException {
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
            logger.info("Modelo de intenção treinado com sucesso a partir dos dados de treino.");
            // Opcional: Salvar o modelo treinado em algum lugar, se necessário, mas a boa prática é tê-lo nos resources.
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