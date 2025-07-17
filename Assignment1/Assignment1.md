Edge-Cloud Architecture Design for IIoT in Smart Oil Refinery ![](Aspose.Words.92ed8721-3521-4227-b3db-d72d13d96078.001.png)

**Group Members:** 

- Vasudev Kishor - CB.SC.U4CSE23151 
- Rachit Anand - CB.SC.U4CSE23139 
- Rohith Abhinav - CB.SC.U4CSE23141 
- RG Shanmugam - CB.SC.U4CSE23257 
1. **Introduction** 

This report explains how to design an Edge-Cloud architecture for a smart oil refinery using Industrial Internet of Things (IIoT) technology. The goal is to make the refinery more efficient, safe, and reliable. The refinery is spread across 5 sq. km and includes units like distillation towers, compressors, and storage tanks. 

2. **Project Goals** 
- Find faults in machines (like pumps and valves) in real-time 
- Predict equipment failures using data from sensors (vibration, temperature, pressure) 
- Monitor worker safety using wearable sensors 
- Detect harmful gas leaks and air quality issues 
- Provide remote monitoring and data dashboards for managers 
3. **System Design Overview** 
1. **Edge Layer (Local Devices in the Field)** 
- Devices Used: Raspberry Pi 4, Jetson Nano, Arduino 
- Sensors: Temperature, Pressure, Vibration, Gas (CH4, CO), GPS 
- Actuators: Emergency shutoff valves, alarms 
- Software: Node-RED for controlling the flow of data, ML Inference using small models 
- Functions: Clean/filter raw data, detect problems using AI, take emergency actions 
2. **Edge Gateway (Communication)** 
- The Edge Gateway manages key data operations before transmitting data to the cloud. 
- Functions performed by Edge Gateway: 
- - Data Preprocessing: Cleans and filters incoming sensor data. 
- - ML Inference (Anomaly Detection): Detects abnormal patterns using lightweight machine learning models. 
- - Real-time Alerts: Generates immediate notifications for critical events. 
- - Buffer & Store Locally: Stores data temporarily in case of network outages. 
- Protocols: MQTT (fast, lightweight), HTTPS (secure) 
- Networks Used: Wi-Fi, 5G, Ethernet 
3. **Cloud Layer (Data Center or Online Platform)** 
- Platform Choices: AWS, Azure, Google Cloud (GCP) 
- Cloud Services: time-series database, analytics, dashboards, OTA updates, SCADA integration 
4. **How It Works (Data Flow)** 
- Sensors collect data and send it to edge devices 
- Edge devices process data and use AI to detect problems 
- Important events are acted upon locally and sent to the cloud 
- Cloud stores and analyzes data, updates models/settings on edge devices 
5. **Edge vs. Cloud Roles** 

Task 

Real-time fault detection Predictive maintenance Safety alerts 

Data visualization Software/model updates 

Edge Device  Cloud Platform Yes  No 

Basic  Advanced 

Yes  No 

No  Yes 

No  Yes 

6. **Key Features** 
- Security: Data is encrypted and access is controlled 
- Low Latency: Fast decision-making at the edge 
- Reliable: Works during network failure with local backup 

**8. Summary** 

This Edge-Cloud architecture helps the refinery run more safely and efficiently. It combines fast edge computing for local actions with powerful cloud tools for analysis, updates, and visualization. It also integrates with existing SCADA systems. This design is scalable, secure, and ideal for real-world IIoT applications in large industrial environments. 

![](Aspose.Words.92ed8721-3521-4227-b3db-d72d13d96078.002.jpeg)
