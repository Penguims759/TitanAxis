// src/main/java/com/titanaxis/service/ai/flows/AbstractConversationFlow.java
package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.ai.ConversationFlow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractConversationFlow implements ConversationFlow {

    protected final Map<String, Step> steps = new LinkedHashMap<>();

    protected static class Step {
        final String question;
        final Predicate<String> validator;
        final String validationErrorMessage;
        final Function<Map<String, Object>, String> dynamicQuestion;

        public Step(String question, Predicate<String> validator, String validationErrorMessage) {
            this.question = question;
            this.validator = validator;
            this.validationErrorMessage = validationErrorMessage;
            this.dynamicQuestion = null;
        }

        public Step(String question) {
            this(question, input -> !input.isEmpty(), "A entrada não pode ser vazia.");
        }

        public Step(Function<Map<String, Object>, String> dynamicQuestion, Predicate<String> validator, String validationErrorMessage) {
            this.question = null;
            this.validator = validator;
            this.validationErrorMessage = validationErrorMessage;
            this.dynamicQuestion = dynamicQuestion;
        }

        // NOVO CONSTRUTOR DE CONVENIÊNCIA
        public Step(Function<Map<String, Object>, String> dynamicQuestion) {
            this(dynamicQuestion, input -> !input.isEmpty(), "A entrada não pode ser vazia.");
        }
    }

    protected abstract void defineSteps();

    @Override
    public AssistantResponse process(String userInput, Map<String, Object> conversationData) {
        defineSteps();

        String lastAskedStep = (String) conversationData.get("lastStep");
        if (lastAskedStep != null && !userInput.isEmpty() && steps.containsKey(lastAskedStep)) {
            Step step = steps.get(lastAskedStep);
            if (step.validator.test(userInput)) {
                conversationData.put(lastAskedStep, userInput);
            } else {
                return new AssistantResponse(step.validationErrorMessage);
            }
        }

        for (Map.Entry<String, Step> entry : steps.entrySet()) {
            String currentStepKey = entry.getKey();
            if (!conversationData.containsKey(currentStepKey)) {
                conversationData.put("lastStep", currentStepKey);
                Step currentStep = entry.getValue();

                String question;
                if (currentStep.dynamicQuestion != null) {
                    question = currentStep.dynamicQuestion.apply(conversationData);
                } else {
                    question = currentStep.question;
                }
                return new AssistantResponse(question);
            }
        }

        return completeFlow(conversationData);
    }

    protected abstract AssistantResponse completeFlow(Map<String, Object> conversationData);
}