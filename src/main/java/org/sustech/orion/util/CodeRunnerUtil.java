package org.sustech.orion.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CodeRunnerUtil {
    private static final String API_URL = "https://api.jdoodle.com/v1/execute";
    private final String clientId;
    private final String clientSecret;

    public CodeRunnerUtil(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String executeCode(String script, String language, String versionIndex) throws IOException {
        // Create connection
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create JSON request body
        String jsonInput = String.format(
                "{\"clientId\":\"%s\"," +
                        "\"clientSecret\":\"%s\"," +
                        "\"script\":\"%s\"," +
                        "\"language\":\"%s\"," +
                        "\"versionIndex\":\"%s\"," +
                        "\"stdin\":\"\"," +
                        "\"compileOnly\":false}",
                clientId, clientSecret, script, language, versionIndex
        );

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }

        connection.disconnect();
        return response.toString();
    }

    public static void main(String[] args) {
        String clientId = "8526e6b9a2854b5b41200265afaa3d48";
        String clientSecret = "6fd62a1fd26b869bb6926ce040c090d8297542faccbf01562648450fb32046de";

        CodeRunnerUtil client = new CodeRunnerUtil(clientId, clientSecret);

        try {
            // Example: Execute a Python program
            String pythonScript = "print('Hello, World!')";
            String result = client.executeCode(pythonScript, "python3", "0");
            System.out.println("API Response: " + result);

        } catch (IOException e) {
            System.err.println("Error executing code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}