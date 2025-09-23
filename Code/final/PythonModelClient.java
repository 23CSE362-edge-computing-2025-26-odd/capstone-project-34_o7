package org.fog.ptsra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * PythonModelClient
 *
 * This class acts as a bridge between the Java iFogSim simulation
 * and the Python Flask API that hosts the Deep Belief Network (DBN) model.
 *
 * Function:
 *   - Takes patient vitals as input (Java Map).
 *   - Sends them to the Python API as JSON.
 *   - Receives prediction results (probability, class, label).
 *   - Wraps results into a PredictionResult object.
 */
public class PythonModelClient {

    // Flask API endpoint where the Python model is running
    private static final String API_URL = "http://127.0.0.1:5000/predict"; 

    /**
     * Sends patient vitals to the Python API and returns the prediction result.
     *
     * @param vitals A map of patient vital signs (HR, SpO2, etc.)
     * @return PredictionResult containing probability, prediction, and label
     */
    public static PredictionResult getPrediction(Map<String, Double> vitals) {
        try {
            // Convert vitals map (Java) → JSON string
            ObjectMapper mapper = new ObjectMapper();
            String jsonInput = mapper.writeValueAsString(vitals);

            // Open HTTP connection to Flask server
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");                          // Send as POST request
            conn.setRequestProperty("Content-Type", "application/json"); // Specify JSON format
            conn.setDoOutput(true);

            // Write JSON request body to output stream
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response from the Flask server
            StringBuilder response;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Parse JSON response into a Map
            Map<String, Object> respMap = mapper.readValue(response.toString(), Map.class);

            // Extract fields from the response
            double prob = ((Number) respMap.get("probability")).doubleValue(); // probability score
            int pred = ((Number) respMap.get("pred")).intValue();              // predicted class (0/1)
            String label = (String) respMap.get("label");                      // label ("Urgent"/"Non-Urgent")

            // Wrap response in PredictionResult and return
            return new PredictionResult(prob, pred, label);

        } catch (Exception e) {
            // If anything goes wrong (e.g., API down), return an error result
            e.printStackTrace();
            return new PredictionResult(0.0, 0, "Error");
        }
    }
}
