
# Architecture Defence Note

Our architecture is designed to be strong and flexible. Even during sudden increases in sensor data, device failures, or network problems, it keeps working well. This is because we’ve added features like fallback mapping for sensors, message priority levels using MQTT, and local decision-making at the edge.

We also make sure that important work is handled by Jetson Nano, while Raspberry Pi takes care of simpler tasks. Buffers and retry methods help in case of data delivery issues. Without changing the overall design, we’ve used smart scheduling and workload distribution to avoid performance issues. So, our system is reliable and ready for industrial use even under stress.
