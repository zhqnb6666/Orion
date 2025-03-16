package org.sustech.orion.AI;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.QianfanV2;
import com.baidubce.qianfan.model.chat.v2.request.RequestV2;
import com.baidubce.qianfan.model.chat.v2.response.ResponseV2;

public class model {
    private static final QianfanV2 client = new Qianfan("bce-v3/ALTAK-rN1GJbJxabN8c2V0twWb5/acf494b1376487f599c2aa78c80f1fe8c06462f9").v2();

    public static String chat(String input){
        RequestV2 request = client.chatCompletion()
                .model("qwq-32b")
                .addMessage("user", input)
                .temperature(0.7)
                .build();
        ResponseV2 response = client.chatCompletion(request);
        return response.getChoices().get(0).getMessage().getContent();
    }
}
