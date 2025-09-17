package org.fog.ptsra;

public class PredictionResult {
    private double probability;
    private int prediction;
    private String label;

    public PredictionResult(double probability, int prediction, String label) {
        this.probability = probability;
        this.prediction = prediction;
        this.label = label;
    }

    public double getProbability() { return probability; }
    public int getPrediction() { return prediction; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        return String.format("PredictionResult{probability=%.4f, prediction=%d, label='%s'}",
                probability, prediction, label);
    }
}
