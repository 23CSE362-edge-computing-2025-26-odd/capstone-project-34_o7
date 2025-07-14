> Edge-Cloud Architecture Design for IIoT in Smart Oil Refinery
>
> **Group** **Members:**
>
> • Vasudev Kishor - CB.SC.U4CSE23151 • Rachit Anand - CB.SC.U4CSE23139
>
> •     Rohith Abhinav - CB.SC.U4CSE23141 •     RG Shanmugam -
> CB.SC.U4CSE23257
>
> **<u>ASSISGMENT-2</u>**
>
> <u>Load Balancing</u>
>
> • **1.** **How** **would** **you** **distribute** **incoming**
> **sensor** **data** **load** **across** **multiple** **edge**
> **devices?**
>
> We distribute sensor data based on **geographical** **zones** **and**
> **sensor** **type** **affinity**. For example, one edge device (e.g.,
> Jetson Nano) is dedicated to high-load areas like distillation towers,
> while others (e.g., Raspberry Pi) handle less intensive zones. We use
> a **local** **load** **balancer** **script** that forwards sensor data
> to the least busy node in that zone using a lightweight broker (e.g.,
> **MQTT** **with** **topic** **filtering**).
>
> • **2.** **What** **strategy** **will** **you** **use** **if** **one**
> **edge** **device** **fails** **or** **becomes** **overloaded?** We
> implement **redundant** **edge** **device** **mapping**. Each sensor
> has a primary and a fallback node assigned. If a device fails
> (detected via heartbeat timeout), data is rerouted to the backup node
> using a **failover** **mechanism** in the edge gateway. Buffering and
> local storage help avoid data loss during rerouting.
>
> <u>Task Scheduling</u>
>
> **3.** **How** **do** **you** **schedule** **different** **edge**
> **tasks** **like** **data** **collection,** **ML** **inference,**
> **and** **communication?**
>
> We use **Node-RED** **with** **custom** **scheduling** **flows** to
> assign priorities and execution intervals.
>
> • Data collection runs at fixed intervals.
>
> • ML inference runs on event-based triggers (e.g., anomaly threshold
> exceeded). • Communication tasks are managed asynchronously with retry
> mechanisms.
>
> This prevents overloading and ensures time-sensitive tasks (like
> alerts) are prioritized.
>
> **4.** **If** **you** **have** **vibration** **and** **gas**
> **sensors** **on** **the** **same** **node,** **how** **would**
> **you** **prioritize** **processing** **under** **constrained**
> **resources?** **Why?**
>
> **Gas** **sensor** **data** **gets** **priority** due to
> safety-critical implications. Gas leaks (e.g., CH4 or CO) pose
> immediate danger, so their data is processed with **higher**
> **priority** **queues** and real-time alert triggers. Vibration data,
> while important, can tolerate slight delays.
>
> <u>Resource Allocation</u>
>
> **5.** **How** **do** **you** **allocate** **tasks** **between**
> **edge** **and** **cloud** **to** **optimize** **latency** **and**
> **bandwidth?**
>
> • **Edge** **handles** real-time ML inference, local decision-making
> (e.g., safety triggers), and data preprocessing.
>
> • **Cloud** **handles** historical analytics, dashboard visualization,
> model training, and OTA updates.
>
> Only **anomalous** **or** **summary** **data** is sent to the cloud,
> significantly reducing bandwidth usage.
>
> **6.** **Given** **limited** **compute** **capacity** **on**
> **Raspberry** **Pi** **vs** **Jetson** **Nano,** **which** **tasks**
> **would** **you** **assign** **to** **each** **and** **why?**
>
> • **Jetson** **Nano** (better GPU): Used for ML inference (e.g.,
> vibration pattern detection), video/image analytics, and moderate-size
> datasets.
>
> • **Raspberry** **Pi**: Used for simpler tasks like temperature/gas
> threshold checks, MQTT communication, and controlling actuators.
>
> This division ensures heavier workloads don't throttle low-power
> devices.
>
> <u>Resilience and Redundancy</u>
>
> **7.** **What** **backup** **or** **reallocation** **strategy**
> **would** **you** **use** **during** **a** **network** **outage**
> **in** **one** **section?**
>
> We implement **local** **storage** **buffers** (e.g., on Raspberry Pi
> SD cards) and **opportunistic** **syncing**. Once the network is
> restored, edge devices automatically sync buffered data with the
> cloud. Safety-related decisions continue locally using pre-deployed
> models.
>
> **8.** **If** **multiple** **edge** **devices** **compete** **for**
> **limited** **bandwidth** **(e.g.,** **shared** **5G),** **how**
> **would** **you** **handle** **communication** **priority?**
>
> We classify messages into **priority** **levels**:
>
> • Critical: Safety alerts (real-time) → Highest QoS • Important:
> Anomaly events → Medium QoS
>
> • Routine: Periodic sensor data → Low QoS
>
> Using **MQTT** **with** **QoS** **+** **priority** **tagging**, only
> high-importance data is allowed during congestion. Additionally, a
> **rate** **limiter** ensures fair bandwidth use.
