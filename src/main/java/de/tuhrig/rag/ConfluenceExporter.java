package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static de.tuhrig.rag.RagConfig.*;

public class ConfluenceExporter {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void exportSpace(String bearerToken) throws IOException, InterruptedException {
        Files.createDirectories(RAW_PAGES);

        int limit = 50;
        int start = 0;

        while (true) {
            var url = CONFLUENCE_BASE_URL +
                    "?spaceKey=" + CONFLUENCE_SPACE_KEY +
                    "&limit=" + limit +
                    "&start=" + start +
                    "&expand=body.storage";

            var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();

            var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new IOException("Confluence returned status " + resp.statusCode() + ": " + resp.body());
            }

            var root = mapper.readTree(resp.body());
            var results = root.get("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                break;
            }

            for (var page : results) {
                var id = page.get("id").asText();
                var outFile = RAW_PAGES.resolve(id + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(outFile.toFile(), page);
            }

            if (results.size() < limit) {
                break;
            }
            start += limit;
        }
    }
}
