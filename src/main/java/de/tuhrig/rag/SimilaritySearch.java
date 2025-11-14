package de.tuhrig.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.tuhrig.rag.RagConfig.EMBEDDINGS;

public class SimilaritySearch {

    private final ObjectMapper mapper = new ObjectMapper();

    public record EmbeddingRecord(String pageId, int chunkIndex, List<Double> vector) {}

    public List<EmbeddingRecord> loadAllEmbeddings() throws IOException {
        var list = new ArrayList<EmbeddingRecord>();

        try (var stream = Files.list(EMBEDDINGS)) {
            for (var path : stream.filter(p -> p.toString().endsWith(".embedding.json")).toList()) {
                var root = mapper.readTree(path.toFile());
                var pageId = root.get("pageId").asText();
                int chunkIndex = root.get("chunkIndex").asInt();
                List<Double> vec = new ArrayList<>();
                for (var n : root.get("embedding")) {
                    vec.add(n.asDouble());
                }
                list.add(new EmbeddingRecord(pageId, chunkIndex, vec));
            }
        }
        return list;
    }

    public double cosine(List<Double> a, List<Double> b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.size(); i++) {
            double x = a.get(i);
            double y = b.get(i);
            dot += x * y;
            na += x * x;
            nb += y * y;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public List<EmbeddingRecord> topK(List<EmbeddingRecord> all, List<Double> query, int k) {
        return all.stream()
                .map(rec -> Map.entry(rec, cosine(rec.vector(), query)))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
