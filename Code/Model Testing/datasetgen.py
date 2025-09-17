import pandas as pd
import numpy as np

np.random.seed(42)

num_samples = 1000

# Generate random features
CPU_req = np.random.randint(500, 3500, size=num_samples)        # 500 to 3500 MIPS
Data_size = np.random.randint(1, 50, size=num_samples)         # 1 to 50 MB
Bandwidth = np.random.randint(10, 200, size=num_samples)       # 10 to 200 Mbps
BT = np.round(np.random.uniform(35.0, 40.0, size=num_samples), 1)  # 35°C to 40°C
BP = np.random.randint(80, 180, size=num_samples)              # 80 to 180 mmHg
ECG = np.random.randint(60, 120, size=num_samples)            # 60 to 120 bpm
Delay_thresh = np.random.randint(100, 1000, size=num_samples) # 100 ms to 1000 ms

# Simple rule to decide:  
# If Delay_thresh < 200 ms or BT > 38.0 → HW (0), else Cloud (1)
Decision = np.where((Delay_thresh < 200) | (BT > 38.0), 0, 1)

# Create DataFrame
df = pd.DataFrame({
    'CPU_req': CPU_req,
    'Data_size': Data_size,
    'Bandwidth': Bandwidth,
    'BT': BT,
    'BP': BP,
    'ECG': ECG,
    'Delay_thresh': Delay_thresh,
    'Decision': Decision
})

# Save to CSV
df.to_csv('synthetic_task_dataset.csv', index=False)

print('Dataset generated with shape:', df.shape)
