Explaining why your architecture is capable  

(or how it was improved). 

\#    Our revised architecture improves load balancing by enabling dynamic sensor-node assignment and failover via MQTT brokers. Task scheduling is optimized using event - driven triggers in Node-RED. We carefully distribute workloads between Jetson Nano and Raspberry Pi to avoid resource bottlenecks. Safety-critical sensors are given higher scheduling and bandwidth priority. Resilience is ensured with local buffering,
