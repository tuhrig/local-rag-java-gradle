package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import static de.tuhrig.rag.RagConfig.*;

public class AzureLlmClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public String chat(String prompt) throws Exception {
        if (AZURE_ENDPOINT == null || AZURE_DEPLOYMENT == null || AZURE_API_KEY == null) {
            throw new IllegalStateException("Azure configuration missing. Set AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_DEPLOYMENT, AZURE_OPENAI_API_KEY.");
        }

        var url = AZURE_ENDPOINT +
                "/openai/deployments/" + AZURE_DEPLOYMENT +
                "/chat/completions?api-version=" + AZURE_API_VERSION;

        var root = mapper.createObjectNode();
        var messages = root.putArray("messages");

        var sys = messages.addObject();
        sys.put("role", "system");
        sys.put("content", "You are an internal assistant. Answer based only on the given context.");

        var user = messages.addObject();
        user.put("role", "user");
        user.put("content", prompt);

        root.put("temperature", 0.0);
        root.put("max_tokens", 800);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", AZURE_API_KEY)
                .POST(BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("LLM error: " + resp.body());
        }

        var json = mapper.readTree(resp.body());
        return json.path("choices").get(0).path("message").path("content").asText();
    }
}
