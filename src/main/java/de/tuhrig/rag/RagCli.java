package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RagCli {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        var search = new SimilaritySearch();
        var llm = new AzureLlmClient();

        System.out.println("Loading embeddings from " + RagConfig.EMBEDDINGS.toAbsolutePath() + " ...");
        var allEmbeddings = search.loadAllEmbeddings();
        System.out.println("Loaded " + allEmbeddings.size() + " embeddings.");

        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n> Question (or 'exit'): ");
            var question = scanner.nextLine();
            if (question == null || question.trim().isEmpty() || "exit".equalsIgnoreCase(question.trim())) {
                break;
            }

            var queryEmbedding = new RagCli.ChunkEmbedderHelper().embedText(question);

            var top = search.topK(allEmbeddings, queryEmbedding, 5);

            var context = top.stream()
                    .map(rec -> loadChunkText(rec.pageId(), rec.chunkIndex()))
                    .collect(Collectors.joining("\n\n"));

            var prompt = "You are an internal assistant. Use only the context below.\n\n" +
                    "### Context\n" + context + "\n\n### Question\n" + question;

            var answer = llm.chat(prompt);
            System.out.println("\n--- Answer ---");
            System.out.println(answer);
        }
    }

    private static String loadChunkText(String pageId, int chunkIndex) {
        var chunkFile = RagConfig.CHUNKS.resolve(pageId + "_chunk_" + chunkIndex + ".json");
        try {
            var root = MAPPER.readTree(chunkFile.toFile());
            return root.path("text").asText("");
        } catch (IOException e) {
            return "[Failed to load chunk " + chunkFile + "]";
        }
    }

    public static class ChunkEmbedderHelper {
        private final ObjectMapper mapper = new ObjectMapper();
        private final java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        public List<Double> embedText(String text) throws IOException, InterruptedException {
            var body = mapper.createObjectNode();
            body.put("text", text);

            var req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(RagConfig.EMBEDDING_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            var resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            var root = mapper.readTree(resp.body());
            var arr = root.get("embedding");
            java.util.List<Double> vec = new java.util.ArrayList<>();
            for (var n : arr) {
                vec.add(n.asDouble());
            }
            return vec;
        }
    }
}
