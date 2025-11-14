package de.tuhrig.rag;

import java.nio.file.Path;

public class RagConfig {

    public static final Path RAW_PAGES     = Path.of("data", "raw_pages");
    public static final Path CLEAN_PAGES   = Path.of("data", "clean_pages");
    public static final Path CHUNKS        = Path.of("data", "chunks");
    public static final Path EMBEDDINGS    = Path.of("data", "embeddings");

    public static final String CONFLUENCE_BASE_URL = "https://your-confluence-domain/rest/api/content";
    public static final String CONFLUENCE_SPACE_KEY = "ABC";

    public static final String EMBEDDING_ENDPOINT = "http://localhost:5005/embed";

    public static final String AZURE_ENDPOINT   = System.getenv("AZURE_OPENAI_ENDPOINT");
    public static final String AZURE_DEPLOYMENT = System.getenv("AZURE_OPENAI_DEPLOYMENT");
    public static final String AZURE_API_KEY    = System.getenv("AZURE_OPENAI_API_KEY");
    public static final String AZURE_API_VERSION = "2024-02-15-preview";

    private RagConfig() {
    }
}
