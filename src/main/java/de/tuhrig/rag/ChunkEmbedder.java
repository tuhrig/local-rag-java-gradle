package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static de.tuhrig.rag.RagConfig.*;

public class ChunkEmbedder {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public void embedAllChunks() throws IOException {
        Files.createDirectories(EMBEDDINGS);
        try (var stream = Files.list(CHUNKS)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .forEach(p -> {
                      try {
                          embedSingleChunk(p);
                      } catch (Exception e) {
                          System.err.println("Failed to embed " + p + ": " + e.getMessage());
                      }
                  });
        }
    }

    private void embedSingleChunk(java.nio.file.Path chunkFile) throws IOException, InterruptedException {
        var root = mapper.readTree(chunkFile.toFile());
        var pageId = root.get("pageId").asText();
        int chunkIndex = root.get("chunkIndex").asInt();
        var text = root.get("text").asText();

        var body = mapper.createObjectNode();
        body.put("text", text);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(EMBEDDING_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Embedding service returned " + resp.statusCode() + ": " + resp.body());
        }

        var embedJson = mapper.readTree(resp.body());

        var out = mapper.createObjectNode();
        out.put("pageId", pageId);
        out.put("chunkIndex", chunkIndex);
        out.set("embedding", embedJson.get("embedding"));

        var outFile = EMBEDDINGS.resolve(pageId + "_chunk_" + chunkIndex + ".embedding.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(outFile.toFile(), out);
    }
}
