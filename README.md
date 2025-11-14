# Local RAG Example (Java 21 + Gradle)

This project demonstrates a fully local Retrieval-Augmented Generation (RAG) pipeline implemented in **Java 21** with **Gradle**.  
It works without any cloud dependencies — all processing happens locally, including embedding generation.

The system performs:

1. **Exporting documents** (example: Confluence REST API)
2. **Cleaning HTML** → plain text  
3. **Chunking documents**  
4. **Generating embeddings locally** (via a Python SentenceTransformers server)
5. **Performing similarity search**
6. **Building an LLM prompt + querying an LLM endpoint**

# 📓 Blog post

You can find my blog post about this example right here:

[https://tuhrig.de/local-rag](https://tuhrig.de/local-rag)

# 🚀 Getting Started

## 1. Install Requirements

### Java & Gradle

- Java **21**
- Gradle

### Python (for embedding server)

```
pip install flask sentence-transformers
```

Optional if not already installed:

```
pip install torch
```

You also need a local embedding model:

📥 **Download model (all-MiniLM-L6-v2):**  

https://www.kaggle.com/datasets/sircausticmail/all-minilm-l6-v2zip

Extract it into e.g.:

```
D:/embedding_models/all-MiniLM-L6-v2/
```

Then update the path in `embedding_service.py`:

```python
MODEL_PATH = r"D:\embedding_models\all-MiniLM-L6-v2"
```

# 🧠 Start the Embedding Server (Python)

Inside your Python environment:

```
python embedding_service.py
```

You should see:

```
Loading model from: D:\embedding_models\all-MiniLM-L6-v2
Embedding server running on http://localhost:5005 ...
```

Your Java code will now POST requests to:

```
http://localhost:5005/embed
```

# 📄 Step-by-Step RAG Pipeline

Below is the **correct order** to run all Java components.

---

## 2. Export Confluence Pages

```java
new ConfluenceExporter().exportSpace("<YOUR_CONFLUENCE_TOKEN>");
```

## 3. Clean HTML + Chunk Documents

```java
new HtmlCleanerAndChunker(600, 100).process();
```

## 4. Generate Embeddings for All Chunks

```java
new ChunkEmbedder().embedAllChunks();
```

## 5. Run Semantic Search + Ask the LLM

```
./gradlew run
```

# 🔌 Optional: LLM Endpoint (Azure OpenAI)

Set these environment variables:

```
export AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
export AZURE_OPENAI_DEPLOYMENT=gpt-4o
export AZURE_OPENAI_API_KEY=your-secret-key
```

# 🧱 Summary

This project gives you a **full, local RAG pipeline**:

✔ Extract internal documents  
✔ Clean them  
✔ Chunk them  
✔ Embed locally  
✔ Perform similarity search  
✔ Send a prompt to your preferred LLM  

Everything runs fully **offline except the final LLM request**.
