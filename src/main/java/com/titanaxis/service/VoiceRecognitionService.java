// penguims759/titanaxis/Penguims759-TitanAxis-e9669e5c4e163f98311d4f51683c348827675c7a/src/main/java/com/titanaxis/service/VoiceRecognitionService.java
package com.titanaxis.service;

import com.titanaxis.util.I18n;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class VoiceRecognitionService {

    private LiveSpeechRecognizer recognizer;
    private volatile boolean listening = false;
    private final Object lock = new Object();
    private boolean available = true;

    public VoiceRecognitionService() {
        try {
            // *** INÍCIO DA ALTERAÇÃO ***
            // Verifica se o idioma atual tem um pacote de voz correspondente.
            // Atualmente, apenas "en-US" está disponível.
            String languageTag = I18n.getCurrentLocale().toLanguageTag();
            if (!"en-US".equalsIgnoreCase(languageTag)) {
                System.err.println("AVISO: Pacote de voz para o idioma '" + languageTag + "' não está disponível. Apenas 'en-US' é suportado.");
                available = false;
                return; // Interrompe a inicialização
            }
            // *** FIM DA ALTERAÇÃO ***

            Configuration configuration = new Configuration();

            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            recognizer = new LiveSpeechRecognizer(configuration);

        } catch (IOException | IllegalStateException e) {
            System.err.println("AVISO: Serviço de reconhecimento de voz não disponível. Causa: " + e.getMessage());
            available = false;
            recognizer = null;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public void startListening(Consumer<String> onResult) {
        if (!available || listening) return;

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
        if (!available) return;
        synchronized (lock) {
            if (!listening) return;
            listening = false;
        }
    }

    public boolean isListening() {
        return listening;
    }
}