package com.titanaxis.service;

import com.google.inject.Singleton;
import com.titanaxis.util.AppLogger;
import com.titanaxis.util.I18n;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import javax.swing.*;
import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;

@Singleton
public class VoiceRecognitionService {

    private LiveSpeechRecognizer recognizer;
    private volatile boolean listening = false;
    private final Object lock = new Object();
    private boolean available = true;
    private static final Logger logger = AppLogger.getLogger();

    public VoiceRecognitionService() {
        // O construtor agora é vazio. A inicialização é feita sob demanda.
    }

    private void initialize() {
        if (recognizer != null) {
            return; // Já inicializado
        }
        try {
            Configuration configuration = new Configuration();
            String lang = I18n.getCurrentLocale().getLanguage();

            if ("pt".equals(lang)) {
                logger.info("A configurar o reconhecimento de voz para Português (pt_BR)...");
                configuration.setAcousticModelPath("resource:/voice/pt_BR/acoustic-model");
                configuration.setDictionaryPath("resource:/voice/pt_BR/dictionary/br-pt.dic");
                configuration.setLanguageModelPath("resource:/voice/pt_BR/language-model/pt_BR.lm");
            } else {
                logger.info("A configurar o reconhecimento de voz para Inglês (en-US)...");
                configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
                configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
                configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
            }

            recognizer = new LiveSpeechRecognizer(configuration);
            available = true;

        } catch (IOException | IllegalStateException e) {
            logger.error("Serviço de reconhecimento de voz não pôde ser inicializado. Causa: " + e.getMessage());
            available = false;
            recognizer = null;
        }
    }

    public boolean isAvailable() {
        if (recognizer == null) {
            initialize();
        }
        return available;
    }

    public void startListening(Consumer<String> onResult) {
        if (!isAvailable() || listening) return;

        synchronized (lock) {
            if (listening) return;
            listening = true;
        }

        new Thread(() -> {
            logger.info("Microfone aberto. A iniciar o reconhecimento de voz...");
            recognizer.startRecognition(true);

            while (listening) {
                SpeechResult result = recognizer.getResult();
                if (result != null) {
                    String hypothesis = result.getHypothesis();
                    if (hypothesis != null && !hypothesis.isEmpty()) {
                        logger.info("Texto reconhecido: " + hypothesis);
                        SwingUtilities.invokeLater(() -> onResult.accept(hypothesis));
                    }
                }
            }
            recognizer.stopRecognition();
            logger.info("Reconhecimento de voz parado.");
        }).start();
    }

    public void stopListening() {
        if (!available) return;
        synchronized (lock) {
            if (!listening) return;
            listening = false;
        }
    }

    public void shutdown() {
        if (recognizer != null) {
            stopListening();
            recognizer = null;
            available = false;
            logger.info("Serviço de reconhecimento de voz desligado e recursos libertados.");
        }
    }

    public boolean isListening() {
        return listening;
    }
}