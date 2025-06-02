package org.sustech.orion.AI;

import org.sustech.orion.AI.ocr_utils.Base64Util;
import org.sustech.orion.AI.ocr_utils.FileUtil;
import org.sustech.orion.AI.ocr_utils.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.io.*;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OCR {
    public static String OCR(String filePath) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
        try {
            // 本地文件路径

            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, StandardCharsets.UTF_8);

            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.3b7ddc8ca2ea1c251a9b1c2e790d60cf.2592000.1751461118.282335-115858441";

            String result = HttpUtil.post(url, accessToken, param);
            return extractText(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractText(String json) {
        StringBuilder result = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode wordsResultNode = rootNode.path("words_result");

            Iterator<JsonNode> elements = wordsResultNode.elements();
            while (elements.hasNext()) {
                JsonNode wordNode = elements.next();
                String word = wordNode.path("words").asText();
                result.append(word).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
