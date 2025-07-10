// src/main/java/com/titanaxis/service/ai/flows/AbstractConversationFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractConversationFlow implements ConversationFlow {

    protected final Map<String, Step> steps = new LinkedHashMap<>();

    protected static class Step {
        final String question;
        // CORREÇÃO: O validador agora recebe o input do utilizador E o mapa de dados da conversa.
        final BiPredicate<String, Map<String, Object>> validator;
        final String validationErrorMessage;
        final Function<Map<String, Object>, String> dynamicQuestion;

        // Construtor para validadores complexos que precisam do contexto
        public Step(String question, BiPredicate<String, Map<String, Object>> validator, String validationErrorMessage) {
            this.question = question;
            this.validator = validator;
            this.validationErrorMessage = validationErrorMessage;
            this.dynamicQuestion = null;
        }

        // Construtor para validadores simples
        public Step(String question, Predicate<String> validator, String validationErrorMessage) {
            this(question, (input, data) -> validator.test(input), validationErrorMessage);
        }

        public Step(String question) {
            this(question, (input, data) -> !input.isEmpty(), "A entrada não pode ser vazia.");
        }

        // Construtor para perguntas dinâmicas com validadores complexos
        public Step(Function<Map<String, Object>, String> dynamicQuestion, BiPredicate<String, Map<String, Object>> validator, String validationErrorMessage) {
            this.question = null;
            this.validator = validator;
            this.validationErrorMessage = validationErrorMessage;
            this.dynamicQuestion = dynamicQuestion;
        }

        // Construtor para perguntas dinâmicas com validadores simples
        public Step(Function<Map<String, Object>, String> dynamicQuestion, Predicate<String> validator, String validationErrorMessage) {
            this(dynamicQuestion, (input, data) -> validator.test(input), validationErrorMessage);
        }

        public Step(Function<Map<String, Object>, String> dynamicQuestion) {
            this(dynamicQuestion, (input, data) -> !input.isEmpty(), "A entrada não pode ser vazia.");
        }
    }

    protected abstract void defineSteps();

    protected abstract AssistantResponse completeFlow(Map<String, Object> conversationData);

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        if (steps.isEmpty()) {
            defineSteps();
        }

        String lastAskedStepKey = (String) conversationData.get("lastStepKey");

        if (lastAskedStepKey != null && !userInput.isEmpty()) {
            Step lastStep = steps.get(lastAskedStepKey);
            // CORREÇÃO: Passa o userInput e o conversationData para o validador.
            if (lastStep != null && !lastStep.validator.test(userInput, conversationData)) {
                return new AssistantResponse(lastStep.validationErrorMessage, Action.AWAITING_INFO, null);
            }
            conversationData.put(lastAskedStepKey, userInput);
        }

        for (Map.Entry<String, Step> entry : steps.entrySet()) {
            String currentStepKey = entry.getKey();
            if (!conversationData.containsKey(currentStepKey)) {
                conversationData.put("lastStepKey", currentStepKey);
                Step currentStep = entry.getValue();
                String question = (currentStep.dynamicQuestion != null)
                        ? currentStep.dynamicQuestion.apply(conversationData)
                        : currentStep.question;

                return new AssistantResponse(question, Action.AWAITING_INFO, null);
            }
        }

        return completeFlow(conversationData);
    }
}