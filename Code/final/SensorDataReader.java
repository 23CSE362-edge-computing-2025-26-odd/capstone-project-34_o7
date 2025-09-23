package org.fog.ptsra;

import org.cloudbus.cloudsim.Log; // CloudSim logging utility
import java.io.*;
import java.util.*;

public class SensorDataReader {

    /**
     * Reads sensor data from a CSV file and returns a list of SensorData objects.
     *
     * @param filePath The path to the CSV file containing sensor data.
     * @return List of SensorData objects.
     */
    public static List<SensorData> readSensorData(String filePath) {
        // List to store all sensor data read from the file
        List<SensorData> sensorDataList = new ArrayList<>();

        // Try-with-resources to ensure BufferedReader is closed automatically
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true; // Flag to skip header line

            // Read the file line by line
            while ((line = br.readLine()) != null) {
                // Skip the first line if it contains column headers
                if (firstLine) { 
                    firstLine = false; 
                    continue; 
                }

                // Split the line by commas
                String[] values = line.split(",");
                
                // Ensure the line has at least 4 values
                if (values.length >= 4) {
                    // Create a new SensorData object
                    SensorData data = new SensorData();
                    
                    // Parse and assign values from CSV
                    data.patientId = values[0].trim(); // Patient ID
                    data.heartRate = Double.parseDouble(values[1].trim()); // Heart Rate
                    data.bloodPressure = Double.parseDouble(values[2].trim()); // Blood Pressure
                    data.glucoseLevel = Double.parseDouble(values[3].trim()); // Glucose Level
                    
                    // Add the data object to the list
                    sensorDataList.add(data);
                }
            }

            // Log the number of records read
            Log.printLine("Read " + sensorDataList.size() + " sensor data records");

        } catch (Exception e) {
            // Catch any exceptions (e.g., file not found, parse errors) and log
            Log.printLine("Error reading sensor data: " + e.getMessage());
        }

        // Return the list of sensor data
        return sensorDataList;
    }

    /**
     * Inner class to represent a single sensor data record.
     */
    public static class SensorData {
        public String patientId;      // Patient ID
        public double heartRate;      // Heart rate value
        public double bloodPressure;  // Blood pressure value
        public double glucoseLevel;   // Glucose level value

        /**
         * Returns a readable string representation of the sensor data.
         */
        @Override
        public String toString() {
            return "Patient: " + patientId + ", HR: " + heartRate +
                   ", BP: " + bloodPressure + ", Glucose: " + glucoseLevel;
        }
    }
}
