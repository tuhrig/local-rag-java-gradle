package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.tuhrig.rag.RagConfig.*;

public class HtmlCleanerAndChunker {

    private final ObjectMapper mapper = new ObjectMapper();
    private final int chunkSize;
    private final int overlap;

    public HtmlCleanerAndChunker(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public void process() throws IOException {
        Files.createDirectories(CLEAN_PAGES);
        Files.createDirectories(CHUNKS);

        try (var stream = Files.list(RAW_PAGES)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .forEach(this::processSinglePage);
        }
    }

    private void processSinglePage(Path jsonFile) {
        try {
            var root = mapper.readTree(jsonFile.toFile());
            var id = root.get("id").asText();
            var body = root.path("body").path("storage").path("value");
            var html = body.isMissingNode() ? "" : body.asText();

            var clean = Jsoup.parse(html).text();
            clean = clean.replaceAll("\s+", " ").trim();

            var cleanPath = CLEAN_PAGES.resolve(id + ".txt");
            Files.writeString(cleanPath, clean);

            createChunks(id, clean);

        } catch (Exception e) {
            System.err.println("Failed to process " + jsonFile + ": " + e.getMessage());
        }
    }

    private void createChunks(String pageId, String text) throws IOException {
        int index = 0;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            var chunk = text.substring(start, end).trim();

            var node = mapper.createObjectNode();
            node.put("pageId", pageId);
            node.put("chunkIndex", index);
            node.put("text", chunk);

            var out = CHUNKS.resolve(pageId + "_chunk_" + index + ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), node);

            index++;
            start = end - overlap;
            if (start < 0) start = 0;
        }
    }
}
