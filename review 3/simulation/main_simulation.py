# Import required libraries for simulation, API communication, and statistical analysis
import simpy
import requests
import statistics
import time
from fog_device import FogDevice

# =============================================================================
# 1. CONFIGURATION
# Define global constants for simulation time, number of devices, and data/task parameters

# =============================================================================
# Simulation parameters

SIMULATION_TIME = 100  # Total simulation time in seconds
NUM_HOSPITAL_WORKSTATIONS = 3
SENSOR_INTERVAL = 5  # Time (seconds) between sensor readings from each sensor

# Task parameters (based on your Java files)
# Each task requires 2000 MIPS and transfers 1MB of data over the network

TASK_PROCESSING_LENGTH = 2000  # MIPS (Million Instructions Per Second) required for a task
TASK_DATA_SIZE = 1000          # Data size in KB for network transmission

# Device specifications (MIPS, Uplink BW in KBps, Latency to Parent in ms)
# Specifications for cloud, gateway, and hospital workstation devices
# Defines their MIPS capacity, bandwidth, and network latency

DEVICE_SPECS = {
    "cloud":   {"mips": 44800, "up_bw": 10000, "latency": 400},
    "gateway": {"mips": 2800,  "up_bw": 10000, "latency": 300},
    "hw":      {"mips": 1000,  "up_bw": 1000,  "latency": 50}
}

# API endpoints for the Flask server
SAFETY_API_URL = "http://127.0.0.1:5000/check_safety"
URGENCY_API_URL = "http://127.0.0.1:5000/predict"

# Network latencies converted from ms to seconds for the simulation
HW_TO_GATEWAY_LATENCY = DEVICE_SPECS["hw"]["latency"] / 1000.0
GATEWAY_TO_CLOUD_LATENCY = DEVICE_SPECS["gateway"]["latency"] / 1000.0
# Total one-way latency from an edge device to the cloud
HW_TO_CLOUD_LATENCY = HW_TO_GATEWAY_LATENCY + GATEWAY_TO_CLOUD_LATENCY

# Global list to collect latency results of completed tasks
completed_tasks_latency = []

# =============================================================================
# 2. PYTHON API CLIENTS
# =============================================================================
def get_safety_check_result(vitals):
    """🛡️ Calls the Flask API to check if the sensor data is safe to use."""
    try:
        response = requests.post(SAFETY_API_URL, json=vitals)
        response.raise_for_status()  # Raise an exception for bad status codes (4xx or 5xx)
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"!!! Safety API Error: {e}. Defaulting to unsafe.")
        return {"is_safe": False}

def get_urgency_prediction(vitals):
    """🧠 Calls the Flask API to get an urgency prediction for the vitals."""
    try:
        response = requests.post(URGENCY_API_URL, json=vitals)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"!!! Urgency API Error: {e}. Returning default non-urgent value.")
        return {"probability": 0.0, "pred": 0, "label": "Error"}

# =============================================================================
# 3. SIMULATION PROCESSES & CONTROLLER LOGIC
# =============================================================================
def execute_task(env, task, target_device, cloud_device):
    """Simulates the full lifecycle of a task on a target device."""
    task_name = f"Task_{task['id']}_from_{task['source']}"
    print(f"{env.now:7.2f}s: {task_name} arrives at {target_device.name} for execution.")

    # Step 1: Request the CPU resource (SimPy handles queuing automatically)
    with target_device.cpu.request() as req:
        yield req  # Wait until the CPU is available

        # Step 2: Simulate Network Transmission Delay to the target device
        network_latency = 0
        if target_device == cloud_device:
            network_latency = HW_TO_CLOUD_LATENCY
        
        if network_latency > 0:
            yield env.timeout(network_latency)

        # Step 3: Simulate Computation Time on the device
        computation_time = TASK_PROCESSING_LENGTH / target_device.mips
        yield env.timeout(computation_time)

    # Step 4: Log completion and record end-to-end latency
    end_time = env.now
    latency = end_time - task['start_time']
    completed_tasks_latency.append(latency)
    print(f"{end_time:7.2f}s: ✅ {task_name} FINISHED on {target_device.name}. Latency: {latency:.4f}s")


def run_ptsra_controller(env, task, local_hw, cloud_device):
    """The 'brain' of the simulation. Decides where to place a task."""
    # 1. Get urgency prediction from the DBN model via API
    prediction = get_urgency_prediction(task['vitals'])
    # Use max(..., 0.01) to avoid division by zero
    urgency_lambda = max(prediction.get("probability", 0.0), 0.01)

    # 2. Estimate processing time on local HW (Edge)
    # Queuing delay is estimated based on current queue length + tasks being processed
    queue_len_hw = len(local_hw.cpu.queue) + len(local_hw.cpu.users)
    queuing_time_hw = queue_len_hw * (TASK_PROCESSING_LENGTH / local_hw.mips)
    computation_time_hw = TASK_PROCESSING_LENGTH / local_hw.mips
    time_hw = (queuing_time_hw / urgency_lambda) + computation_time_hw

    # 3. Estimate processing time on Cloud
    # Uplink bandwidth from the local hardware is used for transmission time
    transmission_time_cloud = TASK_DATA_SIZE / local_hw.up_bw
    computation_time_cloud = TASK_PROCESSING_LENGTH / cloud_device.mips
    time_cloud = (transmission_time_cloud / urgency_lambda) + computation_time_cloud

    # 4. Make the scheduling decision based on PTS-RA logic
    if urgency_lambda > 0.5 or time_hw < time_cloud:
        print(f"{env.now:7.2f}s: PTS-RA -> LOCAL. Urgency(λ)={urgency_lambda:.2f}, EstTime(HW)={time_hw:.2f}s < EstTime(Cloud)={time_cloud:.2f}s")
        return local_hw
    else:
        print(f"{env.now:7.2f}s: PTS-RA -> CLOUD. Urgency(λ)={urgency_lambda:.2f}, EstTime(HW)={time_hw:.2f}s >= EstTime(Cloud)={time_cloud:.2f}s")
        return cloud_device


def sensor(env, name, local_hw, cloud_device):
    """A process that generates a new data task every SENSOR_INTERVAL."""
    task_id = 0
    while True:
        # Hardcoded vitals, as in the original Java file
        vitals = {
            "HR": 120.0, "SpO2": 91.0, "RR": 28.0, "Temp": 39.2,
            "SBP": 185.0, "DBP": 95.0, "HRV": 7.0, "WinMeanHR": 122.0
        }
        
        task = {
            "id": f"{name}-{task_id}",
            "source": name,
            "start_time": env.now,
            "vitals": vitals
        }
        print(f"{env.now:7.2f}s: Sensor '{name}' generated Task_{task['id']}.")

        # Step 1: Perform CNN Safety Check
        safety_result = get_safety_check_result(vitals)

        # Step 2: Conditionally proceed to scheduling if data is safe
        if safety_result.get("is_safe"):
            print(f"{env.now:7.2f}s: 🛡️ Task_{task['id']} is SAFE. Proceeding to scheduling.")
            target_device = run_ptsra_controller(env, task, local_hw, cloud_device)
            env.process(execute_task(env, task, target_device, cloud_device))
        else:
            print(f"{env.now:7.2f}s: ❌ Task_{task['id']} is UNSAFE. Discarding task.")

        # Wait for the next sensor reading interval
        yield env.timeout(SENSOR_INTERVAL)
        task_id += 1

# =============================================================================
# 4. MAIN SIMULATION EXECUTION
# =============================================================================
if __name__ == "__main__":
    print("--- Starting PTS-RA Healthcare Simulation (SimPy) ---")
    start_sim_time = time.time()
    
    # Initialize SimPy environment
    env = simpy.Environment()

    # Create the fog devices from specifications
    cloud = FogDevice(
        env, "cloud", 
        DEVICE_SPECS["cloud"]["mips"], 
        DEVICE_SPECS["cloud"]["up_bw"]
    )
    
    # Create edge devices and start a sensor process for each
    for i in range(NUM_HOSPITAL_WORKSTATIONS):
        hw_name = f"hw-{i+1}"
        hw_device = FogDevice(
            env, hw_name, 
            DEVICE_SPECS["hw"]["mips"], 
            DEVICE_SPECS["hw"]["up_bw"]
        )
        env.process(sensor(env, hw_name, hw_device, cloud))

    # Run the simulation
    env.run(until=SIMULATION_TIME)

    # Print results at the end
    end_sim_time = time.time()
    print("\n--- Simulation Finished ---")
    print(f"Total simulated time: {SIMULATION_TIME:.2f} seconds")
    print(f"Real execution time: {end_sim_time - start_sim_time:.2f} seconds")
    
    if completed_tasks_latency:
        avg_latency = statistics.mean(completed_tasks_latency)
        print(f"Total tasks processed: {len(completed_tasks_latency)}")
        print(f"Average task latency: {avg_latency:.4f} seconds")
    else:
        print("No safe tasks were completed in the simulation time.")
