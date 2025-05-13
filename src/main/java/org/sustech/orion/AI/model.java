package org.sustech.orion.AI;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.QianfanV2;
import com.baidubce.qianfan.model.chat.v2.request.RequestV2;
import com.baidubce.qianfan.model.chat.v2.response.ResponseV2;

import java.util.List;

public class model {
    private static final QianfanV2 client = new Qianfan("bce-v3/ALTAK-rN1GJbJxabN8c2V0twWb5/acf494b1376487f599c2aa78c80f1fe8c06462f9").v2();
    private static final List<String> availableModels = List.of("qwq-32b", "qwen3-32b", "deepseek-r1-distill-qwen-32b", "qwen3-235b-a22b");
    private static final String defaultModel = "qwq-32b";

    public static String chat(String input, String model) {
        String System = "你是一个智能的AI助手，你可以回答任何问题";
        if (!availableModels.contains(model)) {
            model = defaultModel;
        }
        RequestV2 request = client.chatCompletion()
                .model(model)
                .addMessage("system" , System)
                .addMessage("user", input)
                .temperature(0.7)
                .build();
        ResponseV2 response = client.chatCompletion(request);
        return response.getChoices().get(0).getMessage().getContent();
    }

    public static String llm_plagiarism_check(String subbmissionA, String submissionB, String model) {
        return "hi";
    }
}

