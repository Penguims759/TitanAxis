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
    private boolean available = true; // NOVO: Flag para controlar a disponibilidade

    public VoiceRecognitionService() {
        try {
            Configuration configuration = new Configuration();

            // Aponta para os modelos de dados em INGLÊS que vêm com a biblioteca
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            // O erro acontece aqui
            recognizer = new LiveSpeechRecognizer(configuration);

        } catch (IOException | IllegalStateException e) { // ALTERADO: Captura a exceção
            System.err.println("AVISO: Serviço de reconhecimento de voz não disponível. Causa: " + e.getMessage());
            available = false; // NOVO: Marca o serviço como indisponível
            recognizer = null; // NOVO: Garante que o recognizer é nulo
        }
    }

    // NOVO MÉTODO
    public boolean isAvailable() {
        return available;
    }

    public void startListening(Consumer<String> onResult) {
        if (!available || listening) return; // ALTERADO: Verifica a disponibilidade

        synchronized (lock) {
            if (listening) return;
            listening = true;
        }

        new Thread(() -> {
            System.out.println("Microphone open. Start speaking.");
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
        if (!available) return; // ALTERADO: Verifica a disponibilidade
        synchronized (lock) {
            if (!listening) return;
            listening = false;
        }
    }

    public boolean isListening() {
        return listening;
    }
}