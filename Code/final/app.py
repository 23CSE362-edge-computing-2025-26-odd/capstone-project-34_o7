from flask import Flask, request, jsonify
from model_file import load_model, predict  # your DBN model code

app = Flask(__name__)
dbn, scaler, thr = load_model("final_dbn_bfoa_model.pkl")

@app.route("/predict", methods=["POST"])
def predict_api():
    data = request.get_json()
    result = predict(data, dbn, scaler)
    return jsonify(result)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000)
    