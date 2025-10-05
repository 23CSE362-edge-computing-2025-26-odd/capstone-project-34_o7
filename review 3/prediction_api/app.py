from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
import joblib  # Using joblib as it's common for .pkl files

app = Flask(__name__)

# --- 1. Load the DBN Urgency Model ---
# Note: A placeholder 'predict_urgency' function is created as the original
# 'model_file.py' was not provided. You should replace this with your actual logic.
def load_dbn_model(path):
    """Loads the DBN model and associated scaler from a .pkl file."""
    print(f"Loading DBN urgency model from {path}...")
    try:
        # Assuming the .pkl file contains a dictionary or tuple
        with open(path, 'rb') as f:
            model_data = joblib.load(f)
        # Unpack based on how you saved it. This is a common pattern.
        dbn = model_data.get('model')
        scaler = model_data.get('scaler')
        threshold = model_data.get('threshold', 0.5) # Default threshold if not saved
        return dbn, scaler, threshold
    except Exception as e:
        print(f"Error loading DBN model: {e}")
        return None, None, None

def predict_urgency(data, model, scaler):
    """
    Placeholder for your DBN prediction logic.
    Replace this with your actual prediction code.
    """
    # Example logic:
    # 1. Extract features from 'data' dict in the correct order.
    # 2. Scale the features using the 'scaler'.
    # 3. Get prediction from the 'model'.
    # This is a dummy response.
    prob = np.random.rand()
    pred = 1 if prob > 0.5 else 0
    label = "Urgent" if pred == 1 else "Non-Urgent"
    return {"probability": prob, "pred": pred, "label": label}

dbn_model, dbn_scaler, dbn_threshold = load_dbn_model("final_dbn_bfoa_model.pkl")


# --- 2. Load the CNN Safety Model ---
print("Loading CNN safety model from cnn_model.keras...")
try:
    cnn_model = tf.keras.models.load_model("cnn_model.keras")
    print("CNN model loaded successfully.")
except Exception as e:
    print(f"Error loading CNN model: {e}")
    cnn_model = None

# --- 3. Define API Endpoints ---
@app.route("/predict", methods=["POST"])
def predict_urgency_api():
    """Endpoint for the DBN urgency prediction."""
    if not all([dbn_model, dbn_scaler]):
        return jsonify({"error": "DBN model not loaded"}), 500

    data = request.get_json()
    result = predict_urgency(data, dbn_model, dbn_scaler)
    return jsonify(result)


@app.route("/check_safety", methods=["POST"])
def check_safety_api():
    """Endpoint for the CNN data safety check."""
    if cnn_model is None:
        return jsonify({"error": "CNN model not loaded"}), 500

    data = request.get_json()
    
    # Keras model expects a specific input shape.
    # Based on your model file, it's (batch_size, 39, 1).
    # We need to convert the incoming JSON to a NumPy array of this shape.
    # IMPORTANT: The order of features in this list MUST match the order
    # your CNN model was trained on.
    try:
        # This is a placeholder for feature extraction.
        # You must list your 39 features here in the correct order.
        # feature_keys = ['HR', 'SpO2', 'RR', 'Temp', ...] # Example
        # input_data = [data[key] for key in feature_keys]
        
        # As a robust placeholder, we'll take the first 39 values we find.
        # PLEASE UPDATE THIS WITH YOUR ACTUAL FEATURE NAMES.
        input_data = list(data.values())[:39]
        if len(input_data) < 39:
            return jsonify({"error": f"Expected 39 features, but received {len(input_data)}"}), 400

        # Convert to numpy array and reshape for the model
        model_input = np.array(input_data).reshape(1, 39, 1)

        # Get prediction from the model
        prediction = cnn_model.predict(model_input)
        predicted_class = np.argmax(prediction, axis=1)[0]
        
        # Assuming class 1 means "safe" and class 0 means "unsafe"
        is_safe = bool(predicted_class == 1)
        
        return jsonify({"is_safe": is_safe})

    except Exception as e:
        return jsonify({"error": str(e)}), 400


if __name__ == "__main__":
    # Running on port 5000, accessible from the local machine
    app.run(host="127.0.0.1", port=5000)
