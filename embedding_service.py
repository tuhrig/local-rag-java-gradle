from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import logging

app = Flask(__name__)

# -------- CONFIG --------
# Change this to your local model directory
# e.g. D:/embedding_models/all-MiniLM-L6-v2
MODEL_PATH = r"D:/embedding_models/all-MiniLM-L6-v2"

print("Loading model from:", MODEL_PATH)
model = SentenceTransformer(MODEL_PATH)

# Disable verbose logging from Werkzeug
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

@app.route("/embed", methods=["POST"])
def embed():
    try:
        data = request.get_json(force=True)
        text = data.get("text", "")
        if not isinstance(text, str):
            return jsonify({"error": "text must be a string"}), 400

        embedding = model.encode(text).tolist()
        return jsonify({"embedding": embedding})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    print("Embedding server running on http://localhost:5005 ...")
    app.run(host="0.0.0.0", port=5005)
