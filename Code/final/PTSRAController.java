package org.fog.ptsra;

import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.utils.Logger;
import java.util.*;

/**
 * PTSRAController extends the iFogSim Controller.
 * 
 * This class is the "brain" of the system.
 * It decides whether a task (tuple) from a patient sensor
 * should be processed at the edge (hospital workstation)
 * or offloaded to the cloud.
 */
public class PTSRAController extends Controller {
    // Keeps track of queue lengths for each device (Edge/Fog/Cloud)
    private Map<Integer, Integer> deviceQueueLengths = new HashMap<>();

    /**
     * Constructor: initializes controller with fog devices, sensors, actuators.
     * Also initializes queue length for each device = 0.
     */
    public PTSRAController(String name, List<FogDevice> fogDevices,
                           List<Sensor> sensors, List<Actuator> actuators) {
        super(name, fogDevices, sensors, actuators);
        for (FogDevice device : fogDevices) {
            deviceQueueLengths.put(device.getId(), 0);
        }
    }

    /**
     * This method is automatically called whenever a sensor generates data (tuple).
     * It contains the decision-making logic for Edge vs Cloud execution.
     */
    @Override
    protected void processSensorTuple(Tuple tuple) {
        if ("SENSOR_DATA".equals(tuple.getTupleType())) {
            // Patient ID - placeholder (can be linked to CSV later)
            String patientId = "1";

            // Simulated vitals (hardcoded here, but could be read from CSV/real-time input)
            Map<String, Double> vitals = new HashMap<>();
            vitals.put("HR", 120.0);       // Heart Rate
            vitals.put("SpO2", 91.0);      // Oxygen Saturation
            vitals.put("RR", 28.0);        // Respiration Rate
            vitals.put("Temp", 39.2);      // Body Temperature
            vitals.put("SBP", 185.0);      // Systolic Blood Pressure
            vitals.put("DBP", 95.0);       // Diastolic Blood Pressure
            vitals.put("HRV", 7.0);        // Heart Rate Variability
            vitals.put("WinMeanHR", 122.0);// Windowed Mean Heart Rate

            // Call Python ML model (via Flask API) to predict urgency
            PredictionResult result = PythonModelClient.getPrediction(vitals);

            // Urgency level (probability output from model)
            double urgencyLambda = result.getProbability();

            // Identify where tuple originated (local HW) and cloud device
            FogDevice localHw = getDeviceById(tuple.getSourceDeviceId());
            FogDevice cloud = getDeviceByName("cloud");

            // Estimate execution times both locally and in the cloud
            double timeHw = estimateProcessingTimeHw(tuple, localHw, urgencyLambda);
            double timeCloud = estimateProcessingTimeCloud(tuple, localHw, cloud, urgencyLambda);

            FogDevice targetDevice;
            // Scheduling decision:
            // 1. If urgent (Λ > 0.5) → Prefer local HW for low latency
            // 2. Else → Compare estimated times and choose faster
            if (urgencyLambda > 0.5 || timeHw < timeCloud) {
                Logger.debug("PTS-RA", "Scheduling locally, Λ=" + urgencyLambda);
                targetDevice = localHw;
            } else {
                Logger.debug("PTS-RA", "Offloading to cloud, Λ=" + urgencyLambda);
                targetDevice = cloud;
            }

            // Update device queue length
            updateQueueLength(targetDevice.getId(), 1);

            // Send tuple to selected device
            tuple.setDestinationId(targetDevice.getId());
            sendTupleToPlacement(tuple);
        }
    }

    /**
     * Estimate processing time if task runs on local hospital workstation (Edge).
     * Formula: (queueing time / urgency) + computation time
     */
    private double estimateProcessingTimeHw(Tuple tuple, FogDevice hw, double urgencyLambda) {
        int queueLength = deviceQueueLengths.get(hw.getId());
        double queuingTime = queueLength * 0.05; // simple queuing delay model
        double computationTime = tuple.getLength() / hw.getHost().getTotalMips();
        return (queuingTime / urgencyLambda) + computationTime;
    }

    /**
     * Estimate processing time if task is offloaded to Cloud.
     * Formula: (transmission time / urgency) + computation time
     */
    private double estimateProcessingTimeCloud(Tuple tuple, FogDevice hw, FogDevice cloud, double urgencyLambda) {
        double transmissionTime = tuple.getLength() / hw.getUplinkBandwidth();
        double computationTime = tuple.getLength() / cloud.getHost().getTotalMips();
        return (transmissionTime / urgencyLambda) + computationTime;
    }

    /**
     * Update the queue length of a device (increment/decrement).
     */
    private void updateQueueLength(int deviceId, int change) {
        int currentLength = deviceQueueLengths.get(deviceId);
        deviceQueueLengths.put(deviceId, currentLength + change);
    }

    // Utility: get device by name (e.g., "cloud", "hw-1")
    private FogDevice getDeviceByName(String name) {
        for (FogDevice device : getFogDevices()) {
            if (device.getName().equals(name)) return device;
        }
        return null;
    }

    // Utility: get device by ID
    private FogDevice getDeviceById(int id) {
        for (FogDevice device : getFogDevices()) {
            if (device.getId() == id) return device;
        }
        return null;
    }
}
