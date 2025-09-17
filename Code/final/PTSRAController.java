package org.fog.ptsra;

import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.utils.Logger;
import java.util.*;

public class PTSRAController extends Controller {
    private Map<Integer, Integer> deviceQueueLengths = new HashMap<>();

    public PTSRAController(String name, List<FogDevice> fogDevices,
                           List<Sensor> sensors, List<Actuator> actuators) {
        super(name, fogDevices, sensors, actuators);
        for (FogDevice device : fogDevices) {
            deviceQueueLengths.put(device.getId(), 0);
        }
    }

    @Override
    protected void processSensorTuple(Tuple tuple) {
        if ("SENSOR_DATA".equals(tuple.getTupleType())) {
            String patientId = "1"; // placeholder, link to CSV if needed

            Map<String, Double> vitals = new HashMap<>();
            vitals.put("HR", 120.0);
            vitals.put("SpO2", 91.0);
            vitals.put("RR", 28.0);
            vitals.put("Temp", 39.2);
            vitals.put("SBP", 185.0);
            vitals.put("DBP", 95.0);
            vitals.put("HRV", 7.0);
            vitals.put("WinMeanHR", 122.0);

            PredictionResult result = PythonModelClient.getPrediction(vitals);

            double urgencyLambda = result.getProbability();
            FogDevice localHw = getDeviceById(tuple.getSourceDeviceId());
            FogDevice cloud = getDeviceByName("cloud");

            double timeHw = estimateProcessingTimeHw(tuple, localHw, urgencyLambda);
            double timeCloud = estimateProcessingTimeCloud(tuple, localHw, cloud, urgencyLambda);

            FogDevice targetDevice;
            if (urgencyLambda > 0.5 || timeHw < timeCloud) {
                Logger.debug("PTS-RA", "Scheduling locally, Λ=" + urgencyLambda);
                targetDevice = localHw;
            } else {
                Logger.debug("PTS-RA", "Offloading to cloud, Λ=" + urgencyLambda);
                targetDevice = cloud;
            }

            updateQueueLength(targetDevice.getId(), 1);
            tuple.setDestinationId(targetDevice.getId());
            sendTupleToPlacement(tuple);
        }
    }

    private double estimateProcessingTimeHw(Tuple tuple, FogDevice hw, double urgencyLambda) {
        int queueLength = deviceQueueLengths.get(hw.getId());
        double queuingTime = queueLength * 0.05;
        double computationTime = tuple.getLength() / hw.getHost().getTotalMips();
        return (queuingTime / urgencyLambda) + computationTime;
    }

    private double estimateProcessingTimeCloud(Tuple tuple, FogDevice hw, FogDevice cloud, double urgencyLambda) {
        double transmissionTime = tuple.getLength() / hw.getUplinkBandwidth();
        double computationTime = tuple.getLength() / cloud.getHost().getTotalMips();
        return (transmissionTime / urgencyLambda) + computationTime;
    }

    private void updateQueueLength(int deviceId, int change) {
        int currentLength = deviceQueueLengths.get(deviceId);
        deviceQueueLengths.put(deviceId, currentLength + change);
    }

    private FogDevice getDeviceByName(String name) {
        for (FogDevice device : getFogDevices()) {
            if (device.getName().equals(name)) return device;
        }
        return null;
    }

    private FogDevice getDeviceById(int id) {
        for (FogDevice device : getFogDevices()) {
            if (device.getId() == id) return device;
        }
        return null;
    }
}
