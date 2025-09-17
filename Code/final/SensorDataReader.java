package org.fog.ptsra;

import org.cloudbus.cloudsim.Log;
import java.io.*;
import java.util.*;

public class SensorDataReader {

    public static List<SensorData> readSensorData(String filePath) {
        List<SensorData> sensorDataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }

                String[] values = line.split(",");
                if (values.length >= 4) {
                    SensorData data = new SensorData();
                    data.patientId = values[0].trim();
                    data.heartRate = Double.parseDouble(values[1].trim());
                    data.bloodPressure = Double.parseDouble(values[2].trim());
                    data.glucoseLevel = Double.parseDouble(values[3].trim());
                    sensorDataList.add(data);
                }
            }
            Log.printLine("Read " + sensorDataList.size() + " sensor data records");

        } catch (Exception e) {
            Log.printLine("Error reading sensor data: " + e.getMessage());
        }
        return sensorDataList;
    }

    public static class SensorData {
        public String patientId;
        public double heartRate;
        public double bloodPressure;
        public double glucoseLevel;

        @Override
        public String toString() {
            return "Patient: " + patientId + ", HR: " + heartRate +
                   ", BP: " + bloodPressure + ", Glucose: " + glucoseLevel;
        }
    }
}
