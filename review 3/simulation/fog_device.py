# Import SimPy library for event-driven simulation and resource management
import simpy

class FogDevice:
    """
    Represents a fog computing device like the Cloud or a Hospital Workstation.
    
    Each device has a CPU, modeled as a SimPy Resource, which automatically
    handles queuing of tasks if the resource is busy.
    """
    def __init__(self, env, name, mips, up_bw):
        """
        Initializes a FogDevice instance.
        
        Args:
            env (simpy.Environment): The simulation environment.
            name (str): The name of the device (e.g., 'hw-1', 'cloud').
            mips (int): The processing power in Million Instructions Per Second.
            up_bw (int): The uplink bandwidth in KBps.
        """
        self.env = env
        self.name = name
        self.mips = mips
        self.up_bw = up_bw
       # The CPU resource with a capacity of 1 means it can process one task at a time
        self.cpu = simpy.Resource(env, capacity=1)
