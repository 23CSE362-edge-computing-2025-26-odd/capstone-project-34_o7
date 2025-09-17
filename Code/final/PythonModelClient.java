package org.fog.ptsra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class PythonModelClient {

    private static final String API_URL = "http://127.0.0.1:5000/predict"; // Flask API endpoint

    public static PredictionResult getPrediction(Map<String, Double> vitals) {
        try {
            // Convert vitals map to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonInput = mapper.writeValueAsString(vitals);

            // Create connection
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            StringBuilder response;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Parse JSON response
            Map<String, Object> respMap = mapper.readValue(response.toString(), Map.class);
            double prob = ((Number) respMap.get("probability")).doubleValue();
            int pred = ((Number) respMap.get("pred")).intValue();
            String label = (String) respMap.get("label");

            return new PredictionResult(prob, pred, label);

        } catch (Exception e) {
            e.printStackTrace();
            return new PredictionResult(0.0, 0, "Error");
        }
    }
}
