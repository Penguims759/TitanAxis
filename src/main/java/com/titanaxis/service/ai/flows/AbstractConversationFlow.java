package com.titanaxis.service.ai.flows;

import com.titanaxis.model.ai.Action;
import com.titanaxis.model.ai.AssistantResponse;
import com.titanaxis.service.ai.ConversationFlow;

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

        public Step(Function<Map<String, Object>, String> dynamicQuestion) {
            this(dynamicQuestion, input -> !input.isEmpty(), "A entrada não pode ser vazia.");
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
            if (lastStep != null && !lastStep.validator.test(userInput)) {
                return new AssistantResponse(lastStep.validationErrorMessage);
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