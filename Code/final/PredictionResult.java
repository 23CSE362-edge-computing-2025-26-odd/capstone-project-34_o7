package org.fog.ptsra;

/**
 * A simple wrapper class to hold the AI model's prediction result.
 * This makes it easy to pass prediction results between components
 * (like PythonModelClient → PTSRAController).
 */
public class PredictionResult {
    // Probability score returned by the model (0.0 → 1.0)
    private double probability;

    // Predicted class (0 = Non-Urgent, 1 = Urgent)
    private int prediction;

    // Human-readable label for the prediction
    private String label;

    /**
     * Constructor to initialize all fields.
     * @param probability - probability score from model
     * @param prediction  - predicted integer class
     * @param label       - string label ("Urgent" / "Non-Urgent")
     */
    public PredictionResult(double probability, int prediction, String label) {
        this.probability = probability;
        this.prediction = prediction;
        this.label = label;
    }

    // Getters (used in scheduling decisions)
    public double getProbability() { return probability; }
    public int getPrediction() { return prediction; }
    public String getLabel() { return label; }

    /**
     * Converts the object into a human-readable string.
     * Example:
     * PredictionResult{probability=0.8345, prediction=1, label='Urgent'}
     */
    @Override
    public String toString() {
        return String.format("PredictionResult{probability=%.4f, prediction=%d, label='%s'}",
                probability, prediction, label);
    }
}
