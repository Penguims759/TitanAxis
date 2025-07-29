package com.titanaxis.service.ai;

import com.google.inject.Singleton;
import com.titanaxis.service.Intent;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
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
import org.slf4j.Logger;

@Singleton
public class NLPIntentService {

    private static final Logger logger = AppLogger.getLogger();
    private DocumentCategorizerME categorizer;

    public NLPIntentService() {
        loadOrTrainModel();
    }

    private void loadOrTrainModel() {
        // ALTERAÇÃO: Usa o locale completo (ex: "pt_BR") para o nome do ficheiro.
        String localeString = I18n.getCurrentLocale().toString(); // ex: "pt_BR"
        String modelFileName = "nlp-intent-model_" + localeString + ".bin";
        String trainingFileName = "/ai/intent-train_" + localeString + ".txt";

        try {
            File modelFile = new File(modelFileName);
            if (modelFile.exists()) {
                logger.info("A carregar modelo de intenção pré-treinado do ficheiro: " + modelFileName);
                try (InputStream modelIn = new FileInputStream(modelFile)) {
                    DoccatModel model = new DoccatModel(modelIn);
                    this.categorizer = new DocumentCategorizerME(model);
                    logger.info("Modelo de intenção carregado com sucesso.");
                }
            } else {
                logger.info("Nenhum modelo pré-treinado encontrado para o locale '" + localeString + "'. A iniciar treino...");
                trainAndSaveModel(modelFile, trainingFileName, I18n.getCurrentLocale().getLanguage());
            }
        } catch (IOException e) {
            logger.error("Falha crítica ao carregar ou treinar o modelo de intenção para o locale " + localeString, e);
        }
    }

    private void trainAndSaveModel(File modelFile, String trainingFile, String lang) throws IOException {
        try (InputStream dataIn = getClass().getResourceAsStream(trainingFile)) {
            if (dataIn == null) {
                logger.warning("Ficheiro de treino não encontrado: " + trainingFile + ". A usar o ficheiro de treino padrão (pt_BR).");
                trainAndSaveModel(new File("nlp-intent-model_pt_BR.bin"), "/ai/intent-train_pt_BR.txt", "pt");
                return;
            }

            InputStreamFactory inputStreamFactory = () -> dataIn;
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            DoccatModel model = DocumentCategorizerME.train(lang, sampleStream, params, new DoccatFactory());
            this.categorizer = new DocumentCategorizerME(model);
            logger.info("Modelo de intenção para o idioma '" + lang + "' treinado com sucesso.");

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
            logger.warn("Não foi possível determinar a intenção para a entrada: '" + userInput + "'.", e);
            return Intent.UNKNOWN;
        }
    }
}