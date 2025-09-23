package org.fog.ptsra;

import org.cloudbus.cloudsim.Log;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HealthcarePTSRA {
    // Lists to store all fog devices, sensors, and actuators in the simulation
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();

    public static void main(String[] args) {
        Log.printLine("Starting PTS-RA Healthcare Simulation...");

        try {
            // Broker is like a manager that handles communication between application & fog devices
            FogBroker broker = new FogBroker("broker");

            // Build the healthcare application (modules + data flow)
            Application application = createApplication("healthcare_app", broker.getId());

            // Create the fog hierarchy: Cloud → Gateway (fog) → Edge HWs
            createFogDevices(broker.getId());

            // Attach sensors (patients) and actuators (dashboards) to edge devices
            createSensorsAndActuators(broker.getId(), application.getAppId());

            // Controller with PTS-RA scheduling logic (decides edge vs cloud)
            PTSRAController controller = new PTSRAController("ptsra-controller", fogDevices, sensors, actuators);
            controller.setApplication(application);

            // Submit the application to the broker
            broker.submitApplication(application, 0);

            // Start simulation time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Launch controller (which starts the simulation loop)
            controller.start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Error occurred during simulation");
        }
    }

    /**
     * Builds the application graph (modules + edges + tuple mappings)
     */
    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        // Two processing modules
        application.addAppModule("urgency_calculator", 10); // Runs AI model
        application.addAppModule("data_processor", 10);     // Processes results

        // Edges define the flow of data (tuples)
        application.addAppEdge("SENSOR", "urgency_calculator", 1000, 500,
                "SENSOR_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("urgency_calculator", "data_processor", 1000, 500,
                "URGENCY_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("data_processor", "ACTUATOR", 100, 50,
                "PROCESSED_DATA", Tuple.DOWN, AppEdge.ACTUATOR);

        // Define how tuples are transformed
        application.addTupleMapping("urgency_calculator", "SENSOR_DATA", "URGENCY_DATA",
                new FractionalSelectivity(1.0));
        application.addTupleMapping("data_processor", "URGENCY_DATA", "PROCESSED_DATA",
                new FractionalSelectivity(1.0));

        // Define the application loop: SENSOR → urgency_calculator → data_processor → ACTUATOR
        List<String> loop = new ArrayList<String>();
        loop.add("SENSOR");
        loop.add("urgency_calculator");
        loop.add("data_processor");
        loop.add("ACTUATOR");
        application.addAppLoop(new AppLoop(loop));

        return application;
    }

    /**
     * Creates the hierarchy of fog devices:
     * Cloud → Gateway (fog router) → Hospital Workstations (edge)
     */
    private static void createFogDevices(int brokerId) {
        // 🌐 Cloud device (high resources, high latency)
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000,
                0.01, 16*103, 16*83.25);
        cloud.setParentId(-1);   // Root node (no parent)
        fogDevices.add(cloud);

        // ☁ Gateway device (fog layer, medium resources)
        FogDevice gateway = createFogDevice("gateway", 2800, 4000, 10000, 10000,
                0.0, 4*103, 4*83.25);
        gateway.setParentId(cloud.getId());   // Connects to cloud
        gateway.setUplinkLatency(400);        // Simulates WAN latency
        fogDevices.add(gateway);

        // 🏥 Hospital workstations (edge devices, lower resources)
        for (int i = 1; i <= 3; i++) {
            FogDevice hw = createFogDevice("hw-" + i, 1000, 1000, 10000, 10000,
                    0.0, 2*103, 2*83.25);
            hw.setParentId(gateway.getId());   // Connects to gateway
            hw.setUplinkLatency(300);          // Simulates LAN/WiFi latency
            fogDevices.add(hw);
        }
    }

    /**
     * Helper function to build a fog device with given specs
     */
    private static FogDevice createFogDevice(String nodeName, long mips, int ram,
            long upBw, long downBw, double ratePerMips, double busyPower, double idlePower) {

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "x86", "Linux", "Xen", mips, ram, upBw, downBw, busyPower, idlePower);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new FogLinearPowerModel(busyPower, idlePower), ratePerMips, 0, 0, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fogdevice;
    }

    /**
     * Creates sensors (input data sources) and actuators (output dashboards)
     */
    private static void createSensorsAndActuators(int brokerId, String appId) {
        for (int i = 1; i <= 3; i++) {
            FogDevice hw = getDeviceByName("hw-" + i);

            // Each HW has one sensor that generates vitals
            Sensor sensor = new Sensor("sensor-" + i, "SENSOR_DATA", brokerId,
                    appId, new DeterministicDistribution(5)); // new tuple every 5 units
            sensor.setGatewayDeviceId(hw.getId());
            sensor.setLatency(50.0);  // Sensor transmission latency
            sensors.add(sensor);

            // Each HW also has one actuator (doctor dashboard)
            Actuator actuator = new Actuator("dashboard-" + i, brokerId, appId, "ACTUATOR");
            actuator.setGatewayDeviceId(hw.getId());
            actuator.setLatency(2.0); // Dashboard display latency
            actuators.add(actuator);
        }
    }

    /**
     * Utility to fetch a fog device by name
     */
    private static FogDevice getDeviceByName(String name) {
        for (FogDevice dev : fogDevices) {
            if (dev.getName().equals(name)) {
                return dev;
            }
        }
        return null;
    }
}
