// src/main/java/com/titanaxis/service/VoiceRecognitionService.java
package com.titanaxis.service;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import javax.swing.*;
import java.io.IOException;
import java.util.function.Consumer;

public class VoiceRecognitionService {

    private LiveSpeechRecognizer recognizer;
    private volatile boolean listening = false;
    private final Object lock = new Object();

    public VoiceRecognitionService() {
        try {
            Configuration configuration = new Configuration();

            // Aponta para os modelos de dados em INGLÊS que vêm com a biblioteca
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            recognizer = new LiveSpeechRecognizer(configuration);

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar os modelos internos da Sphinx-4.");
            throw new RuntimeException(e);
        }
    }

    public void startListening(Consumer<String> onResult) {
        if (listening) return;

        synchronized (lock) {
            if (listening) return;
            listening = true;
        }

        new Thread(() -> {
            System.out.println("Microphone open. Start speaking (in English for this test).");
            recognizer.startRecognition(true);

            SpeechResult result;
            while (listening && (result = recognizer.getResult()) != null) {
                String hypothesis = result.getHypothesis();
                if (hypothesis != null && !hypothesis.isEmpty()) {
                    System.out.println("Recognized text: " + hypothesis);
                    SwingUtilities.invokeLater(() -> onResult.accept(hypothesis));
                }
            }

            recognizer.stopRecognition();
            System.out.println("Speech recognition stopped.");
        }).start();
    }

    public void stopListening() {
        synchronized (lock) {
            if (!listening) return;
            listening = false;
        }
    }

    public boolean isListening() {
        return listening;
    }
}