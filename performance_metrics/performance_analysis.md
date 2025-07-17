
# Performance Metrics Analysis

**Group Members:**

- Vasudev Kishor - CB.SC.U4CSE23151  
- Rachit Anand - CB.SC.U4CSE23139  
- Rohith Abhinav - CB.SC.U4CSE23141  
- RG Shanmugam - CB.SC.U4CSE23257

---

## ASSIGNMENT-3

### 1. Throughput

Our architecture tries to maintain high throughput by using multiple edge devices like Jetson Nano and Raspberry Pi placed in different zones of the refinery. Each device works independently to collect and process sensor data, which helps avoid delays and improves data flow. We're using MQTT with topic filtering, which is a lightweight protocol that handles lots of messages efficiently.

To support high throughput, we rely on:

- Running multiple tasks at the same time using Node-RED.
- Buffering messages in MQTT so nothing gets lost or stuck.
- Dividing sensors by zones to reduce the load on any one device.
- Having a fallback plan in case one edge node fails.

**Estimated throughput:**

- Jetson Nano: about 250 to 500 messages per second  
- Raspberry Pi: about 100 to 200 messages per second  
- Total system throughput: roughly 1000 to 1500 messages per second if we have 5 devices

---

### 2. Latency

We estimate that the time between collecting sensor data and responding to it is around 100 to 200 milliseconds for urgent cases like gas leaks. For less critical tasks, it may go up to a second.

The main things that add to latency are:

- Delays when data travels over the network to the cloud  
- Time taken by the cloud to analyze data  
- Time taken by ML models to run on edge devices like Jetson Nano

**To reduce or manage latency, we:**

- Run ML models directly on edge devices for quick decision-making  
- Trigger tasks only when thresholds are crossed (event-based)  
- Use MQTT with quality of service (QoS) for faster delivery of important messages  
- Keep processing local when there's a network issue

---

### 3. Energy Efficiency

Our setup is fairly energy-efficient. Raspberry Pi uses about 3 to 5 watts and Jetson Nano about 5 to 10 watts depending on the tasks. We try to use each device for what it's good at, so we don’t waste energy.

**Ways we save energy include:**

- Assigning complex tasks to Jetson Nano and simple ones to Raspberry Pi  
- Letting sensors sleep when not needed  
- Filtering and processing data locally so less data needs to be sent to the cloud

**Trade-offs:**

- Real-time processing may use more power, but it’s important for safety  
- Delaying ML processing saves energy, but it might slow down response for some events
