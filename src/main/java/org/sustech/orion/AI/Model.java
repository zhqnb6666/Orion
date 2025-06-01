package org.sustech.orion.AI;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;

import java.util.Arrays;
import java.util.List;

import static org.sustech.orion.AI.PromptBuilder.build_grading_prompt;
import static org.sustech.orion.AI.PromptBuilder.build_plagiarism_prompt;

public class Model {
    private static final String API_KEY = "sk-Jl53h2087p93OXhlD97c7qQZal36QGxQTGMGOOZgUCx8un0z";
    private static final String BASE_URL = "https://xiaoai.plus/v1";

    private static final List<String> availableModels = List.of("gpt-4o", "gpt-4o-mini");
    private static final String defaultModel = "gpt-4o";

    private static ChatLanguageModel createChatModel(String modelName) {
        return OpenAiChatModel.builder()
                .apiKey(API_KEY)
                .baseUrl(BASE_URL)
                .modelName(modelName)
                .temperature(0.6)
                .build();
    }

    public static String chat(String input, String model) {
        String systemPrompt = "你是一个智能的AI助手，你可以回答任何问题";

        if (!availableModels.contains(model)) {
            model = defaultModel;
        }

        ChatLanguageModel chatModel = createChatModel(model);

        Response<AiMessage> response = chatModel.generate(
                SystemMessage.from(systemPrompt),
                UserMessage.from(input)
        );

        return response.content().text();
    }

    public static String llm_plagiarism_check(String submissionA, String submissionB, String model) {
        String systemPrompt = "你是一个智能的AI助手，你可以回答任何问题";

        if (!availableModels.contains(model)) {
            model = defaultModel;
        }

        String input = build_plagiarism_prompt(submissionA, submissionB);
        ChatLanguageModel chatModel = createChatModel(model);

        Response<AiMessage> response = chatModel.generate(
                SystemMessage.from(systemPrompt),
                UserMessage.from(input)
        );

        return response.content().text();
    }

    public static String llm_grading(String Question, String Answer, String model) {
        String systemPrompt = "你是一个智能的AI助手，你可以回答任何问题";

        if (!availableModels.contains(model)) {
            model = defaultModel;
        }

        String input = build_grading_prompt(Question, Answer);
        ChatLanguageModel chatModel = createChatModel(model);

        Response<AiMessage> response = chatModel.generate(
                SystemMessage.from(systemPrompt),
                UserMessage.from(input)
        );

        return response.content().text();
    }
}