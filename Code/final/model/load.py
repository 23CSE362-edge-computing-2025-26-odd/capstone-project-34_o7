import numpy as np
import joblib, time
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, accuracy_score, confusion_matrix, classification_report
from imblearn.over_sampling import SMOTE
import torch, torch.nn as nn
from torch.utils.data import TensorDataset, DataLoader

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

class RBM(nn.Module):
    def __init__(self, n_vis, n_hid, k=1):
        super().__init__()
        self.n_vis = n_vis
        self.n_hid = n_hid
        self.k = k
        self.W = nn.Parameter(torch.randn(n_hid, n_vis) * 0.01)
        self.h_bias = nn.Parameter(torch.zeros(n_hid))
        self.v_bias = nn.Parameter(torch.zeros(n_vis))
    
    def sample_h(self, v):
        p_h = torch.sigmoid(torch.matmul(v, self.W.t()) + self.h_bias)
        return p_h, torch.bernoulli(p_h)

    def forward(self, v):
        ph, _ = self.sample_h(v)
        return ph

class DBN(nn.Module):
    def __init__(self, layer_sizes, rbm_k=1):
        super().__init__()
        self.layer_sizes = layer_sizes
        self.rbms = nn.ModuleList([RBM(layer_sizes[i], layer_sizes[i+1], k=rbm_k)
                                   for i in range(len(layer_sizes)-1)])
        last = layer_sizes[-1]
        self.mlp = nn.Sequential(
            nn.Linear(last, max(32, last//2)),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(max(32, last//2), 1)
        )
    def predict_proba(self, X):
        x = torch.tensor(X, dtype=torch.float32).to(device)
        with torch.no_grad():
            h = x
            for rbm in self.rbms:
                h = rbm.forward(h)
            logits = self.mlp(h).cpu().numpy().flatten()
        probs = 1.0/(1.0+np.exp(-logits))
        return probs

def load_model(path='final_dbn_bfoa_model.pkl'):
    pkg = joblib.load(path)
    dbn = DBN(pkg['layer_sizes'])
    dbn.load_state_dict(pkg['dbn_state_dict'])
    dbn.eval()
    return dbn, pkg['scaler'], pkg['threshold']

def predict(sample, dbn, scaler):
    feature_names = ["HR","SpO2","RR","Temp","SBP","DBP","HRV","WinMeanHR"]
    arr = np.array([sample[k] for k in feature_names], dtype=float).reshape(1, -1)
    arr_s = scaler.transform(arr)
    prob = dbn.predict_proba(arr_s)[0]
    pred = int(prob >= 0.5)
    label = "Urgent" if pred == 1 else "Non-Urgent"
    return {"probability": float(prob), "pred": pred, "label": label}

if __name__ == "__main__":
    dbn, scaler, thr = load_model("final_dbn_bfoa_model.pkl")

    sample = {"HR":120, "SpO2":91, "RR":28, "Temp":39.2,
              "SBP":185, "DBP":95, "HRV":7, "WinMeanHR":122}

    result = predict(sample, dbn, scaler)
    print("Input:", sample)
    print("Prediction:", result)
    print("Expected: Urgent")
