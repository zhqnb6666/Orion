package org.sustech.orion.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


@Component
public class CodeRunnerUtil {
    private static final String EXECUTE_API_URL = "https://api.jdoodle.com/v1/execute";
    private static final String CREDIT_CHECK_API_URL = "https://api.jdoodle.com/v1/credit-spent";
    private static final int CREDIT_LIMIT = 20;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final List<ApiCredential> credentials = new ArrayList<>();
    private int currentCredentialIndex = 0;

    @Value("${jdoodle.client-id-1}")
    private String clientId1;

    @Value("${jdoodle.client-secret-1}")
    private String clientSecret1;

    @Value("${jdoodle.client-id-2}")
    private String clientId2;

    @Value("${jdoodle.client-secret-2}")
    private String clientSecret2;

    @Value("${jdoodle.client-id-3}")
    private String clientId3;

    @Value("${jdoodle.client-secret-3}")
    private String clientSecret3;

    @Value("${jdoodle.client-id-4}")
    private String clientId4;

    @Value("${jdoodle.client-secret-4}")
    private String clientSecret4;

    @Value("${jdoodle.client-id-5}")
    private String clientId5;

    @Value("${jdoodle.client-secret-5}")
    private String clientSecret5;

    public CodeRunnerUtil() {
    }

    @PostConstruct
    public void init() {
        credentials.add(new ApiCredential(clientId1, clientSecret1));
        credentials.add(new ApiCredential(clientId2, clientSecret2));
        credentials.add(new ApiCredential(clientId3, clientSecret3));
        credentials.add(new ApiCredential(clientId4, clientSecret4));
        credentials.add(new ApiCredential(clientId5, clientSecret5));
    }

    public String executeCode(String script, String language, String versionIndex, String stdin) throws IOException {
        // 检查当前凭证的使用情况并在需要时切换
        checkAndSwitchCredentialIfNeeded();
        ApiCredential credential = credentials.get(currentCredentialIndex);

        // 创建连接
        URL url = new URL(EXECUTE_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 创建 JSON 请求体
        String jsonInput = String.format(
                "{\"clientId\":\"%s\"," +
                        "\"clientSecret\":\"%s\"," +
                        "\"script\":\"%s\"," +
                        "\"language\":\"%s\"," +
                        "\"versionIndex\":\"%s\"," +
                        "\"stdin\":\"%s\"," +
                        "\"compileOnly\":false}",
                credential.clientId, credential.clientSecret,
                escapeJson(script), language, versionIndex, escapeJson(stdin)
        );
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        } catch (IOException e) {
            // 如果请求失败，尝试读取错误流
            try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            throw new IOException("执行代码失败: " + response, e);
        }

        connection.disconnect();
        return response.toString();
    }

    private void checkAndSwitchCredentialIfNeeded() throws IOException {
        ApiCredential currentCredential = credentials.get(currentCredentialIndex);
        int usedCredits = checkCreditUsage(currentCredential);
        if (usedCredits >= CREDIT_LIMIT) {
            int nextIndex = currentCredentialIndex + 1;
            if (nextIndex >= credentials.size()) {
                throw new IOException("警告：所有 API 凭证今日已达使用限制，请明天再试");
            }
            currentCredentialIndex = nextIndex;
        }
    }

    private int checkCreditUsage(ApiCredential credential) throws IOException {
        URL url = new URL(CREDIT_CHECK_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInput = String.format(
                "{\"clientId\":\"%s\",\"clientSecret\":\"%s\"}",
                credential.clientId, credential.clientSecret
        );

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }

        connection.disconnect();

        // 解析响应获取已使用的 credit
        JsonNode jsonNode = objectMapper.readTree(response.toString());
        return jsonNode.get("used").asInt();
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // 内部类：API 凭证
    private static class ApiCredential {
        private final String clientId;
        private final String clientSecret;

        public ApiCredential(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }
    }
}